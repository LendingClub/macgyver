package io.macgyver.discovery.agent;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public abstract class Sender {

	Properties props = new Properties();
	int interval = 60;

	ScheduledExecutorService executor;

	MacGyverDiscoveryAgent agent;

	java.util.logging.Logger log = java.util.logging.Logger
			.getLogger(Sender.class.getName());

	
	public final void configure(Properties p) {
		props.putAll(p);
		
		String iv = p.getProperty("interval");
		if (iv!=null) {
			interval = Integer.parseInt(iv);
		}
	}
	
	public abstract void send(Properties p) throws IOException;

	public static String toFormEncodedString(Properties p) {
		try {
			StringBuffer sb = new StringBuffer();

			for (Map.Entry entry : p.entrySet()) {
				String key = entry.getKey().toString();
				String val = entry.getValue().toString();
				if (sb.length() > 0) {
					sb.append("&");
				}
				sb.append(URLEncoder.encode(key, "UTF-8"));
				sb.append("=");
				sb.append(URLEncoder.encode(val, "UTF-8"));
			}

			return sb.toString();
		} catch (UnsupportedEncodingException e) {
			// URLEncoder.encode() throwing UnsupportedEncodingException is the world's stupidest exception declaration
			throw new IllegalArgumentException(e);
		}
	}

	public void start() {

		if (executor != null) {
			throw new IllegalStateException("start() already called");
		}
		Runnable r = new Runnable() {

			@Override
			public void run() {
				try {
					log.finer(Sender.this+" sending...");
					send(agent.collect());
				} catch (Exception e) {
					log.log(java.util.logging.Level.WARNING,"problem",e);
				}
			}

		};
		executor = new ScheduledThreadPoolExecutor(1);
		
		log.info("scheduling "+this+" to report every "+interval+" seconds");
		executor.scheduleWithFixedDelay(r, 0, interval, TimeUnit.SECONDS);
	}

	public void stop() {
		if (executor != null) {
			try {
				executor.shutdown();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
