package io.macgyver.plugin.elb.a10;

import io.macgyver.core.util.WeakRefScheduler;
import io.macgyver.plugin.elb.ElbException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.UncheckedExecutionException;

public class A10HAClientImpl implements A10Client, Runnable {

	private Logger logger = LoggerFactory.getLogger(A10HAClientImplTest.class);

	protected List<A10Client> clients = Lists.newCopyOnWriteArrayList();

	protected LoadingCache<String, A10Client> cache;

	public static final int DEFAULT_NODE_CHECK_SECS = 60;
	private boolean certVerificationEnabled = false;

	private static WeakRefScheduler daemon = new WeakRefScheduler(3);

	public static String ACTIVE_KEY = "ACTIVE_KEY";

	public A10HAClientImpl() {
		this(DEFAULT_NODE_CHECK_SECS, false);

	}

	public A10HAClientImpl(int nodeCheckSecs, boolean certVerificationEnabled) {
		cache = CacheBuilder.newBuilder()
				.expireAfterWrite(nodeCheckSecs, TimeUnit.SECONDS)
				.build(new ClientSelector());
		this.certVerificationEnabled = false;

		int interval = Math.max(10, nodeCheckSecs - 10);
		daemon.scheduleWithFixedDelay(this, interval, interval,
				TimeUnit.SECONDS);

	}

	public A10HAClientImpl(String urlList, String username, String password) {
		this(urlList, username, password, DEFAULT_NODE_CHECK_SECS, false);
	}

	public A10HAClientImpl(String urlList, String username, String password,
			int nodeCheckSecs, boolean certVerificationEnabled) {

		this(nodeCheckSecs, certVerificationEnabled);

		StringTokenizer st = new StringTokenizer(urlList, " ;,");
		while (st.hasMoreTokens()) {
			String url = st.nextToken().trim();
			logger.info("adding A10 url to HA client config: {}", url);
			A10ClientImpl c = new A10ClientImpl(url, username, password);
			clients.add(c);
		}
	}

	public class ClientSelector extends CacheLoader<String, A10Client> {

		@Override
		public A10Client load(String ignore) throws Exception {
			// This is a synchronous check...we don't realy want to see many of
			// these
			logger.debug("sync check for active A10...");
			return findActiveA10();
		}

	}

	public A10Client getActiveClient() {

		try {
			return cache.get(ACTIVE_KEY);
		} catch (ExecutionException e) {
			throw new ElbException(e);
		} catch (UncheckedExecutionException e) {
			Throwable t = e.getCause();

			if (t != null && t instanceof RuntimeException) {
				throw ((RuntimeException) t);
			}
			if (t != null) {
				throw new ElbException(e);
			}
			throw e;
		}
	}

	protected synchronized void ensureClientIsFirstInList(A10Client c) {
		if (clients.isEmpty()) {
			clients.add(c);
		} else {
			A10Client currentHead = clients.get(0);
			if (c == currentHead) {
				logger.debug("already at head: {}", c);
				// nothing to do
			} else {
				logger.info("shifting client to head of list: {}", c);
				clients.remove(c);
				clients.add(0, c);
			}
		}
	}

	protected A10Client findActiveA10() {

		logger.debug("searching for active A10");

		List<A10Client> tmp = Lists.newArrayList(clients);
		for (A10Client c : tmp) {
			try {
				if (c.isActive()) {
					logger.debug("active: {}", c);

					ensureClientIsFirstInList(c);
					cache.put(ACTIVE_KEY, c);
					return c;
				} else {
					logger.debug("inactive: {}", c);
				}

			} catch (Exception e) {
				logger.warn(
						"problem determining if node is active: "
								+ c.toString(), e);
			}

		}
		throw new ElbException("active A10 not found in (" + clients + ")");
	}

	@Override
	public ObjectNode getServiceGroupAll() {
		return getActiveClient().getServiceGroupAll();
	}

	@Override
	public ObjectNode getSystemPerformance() {
		return getActiveClient().getSystemPerformance();
	}

	@Override
	public ObjectNode getSystemInfo() {
		return getActiveClient().getSystemInfo();
	}

	@Override
	public ObjectNode getDeviceInfo() {
		return getActiveClient().getDeviceInfo();
	}

	@Deprecated
	public ObjectNode invoke(String method, Map<String, String> params) {
		return invokeJson(method, params);
	}

	@Deprecated
	public ObjectNode invoke(String method, String... args) {
		return invokeJson(method, args);
	}

	public ObjectNode invokeJson(String method, Map<String, String> params) {
		return getActiveClient().invoke(method, params);
	}

	public ObjectNode invokeJson(String method, String... args) {
		return getActiveClient().invoke(method, args);
	}

	@Override
	public Element invokeXml(String method, String... args) {
		return getActiveClient().invokeXml(method, args);
	}

	@Override
	public Element invokeXml(String method, Map<String, String> args) {
		return getActiveClient().invokeXml(method, args);
	}

	@Override
	public boolean isActive() {
		return getActiveClient().isActive();
	}

	public void resetClientHAStatus() {
		cache.invalidateAll();
	}

	public void run() {
		try {
			logger.debug("performing maintenance on {}", this);
			findActiveA10();
		} catch (Exception e) {
			logger.warn("", e);
		}
	}

}
