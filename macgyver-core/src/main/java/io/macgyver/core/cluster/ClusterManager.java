/**
 * Copyright 2017 Lending Club, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.macgyver.core.cluster;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.lendingclub.neorx.NeoRxClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class ClusterManager implements ApplicationListener<ApplicationReadyEvent> {

	public class NodeInfo {
		ObjectNode data;

		public NodeInfo(ObjectNode n) {
			this.data = n;
		}

		public JsonNode getData() {
			return data;
		}

		public String getId() {
			return data.path("id").asText();
		}

		public boolean isPrimary() {
			return data.path("primary").asBoolean(false);
		}

		public long getUpdateTs() {
			return data.path("updateTs").asLong(0);
		}

		public long getCreateTs() {
			return data.path("createTs").asLong(0);
		}

		public boolean isSelf() {
			return data.path("self").asBoolean(false);
		}

		public String toString() {
			return data.toString();
		}

		public String getHost() {
			return data.path("host").asText();
		}
	}

	final String uuid = UUID.randomUUID().toString();

	@Autowired
	NeoRxClient neo4j;

	static Logger logger = LoggerFactory.getLogger(ClusterManager.class);

	ScheduledExecutorService scheduler;

	long scanIntervalSecs = 10;

	AtomicBoolean primaryStatus = new AtomicBoolean(false);

	AtomicReference<Map<String, NodeInfo>> clusterNodeMapRef = new AtomicReference<Map<String, NodeInfo>>(
			ImmutableMap.of());

	AtomicReference<Supplier<Boolean>> leaderEligibilitySupplierRef = new AtomicReference<>(new DefaultEligibility());

	class DefaultEligibility implements Supplier<Boolean> {

		private final boolean ALWAYS_ELIGIBLE=true;
		
		@Override
		public Boolean get() {
			return ALWAYS_ELIGIBLE;
		}

	}


	public List<String> getProcessIdList() {
		return ImmutableList.copyOf(getClusterNodes().keySet());
	}

	public String getLocalProcessId() {
		return uuid;
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent arg0) {

		ensureUniqueConstraints();

		logger.info("starting ClusterManager...");
		ThreadFactory tf = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ClusterManager-%s").build();
		scheduler = Executors.newSingleThreadScheduledExecutor(tf);

		Runnable r = new Runnable() {

			@Override
			public void run() {
				try {
					reportStatus();
				} catch (Exception e) {
					logger.warn("problem determining cluster status", e);
				}
			}

		};

		long initialDelay = TimeUnit.SECONDS.toSeconds(0);
		scheduler.scheduleWithFixedDelay(r, initialDelay, scanIntervalSecs, TimeUnit.SECONDS);

		logger.info("...started");
	}

	public boolean isLeader() {
		return isPrimary();
	}

	public boolean isPrimary() {
		return primaryStatus.get();
	}

	public String getLocalHost() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			return "localhost";
		}
	}

	@SuppressWarnings("unchecked")
	public void reportStatus() {

		String cypher = "merge (cn:ClusterNode {id:{id}}) ON MATCH set cn.updateTs=timestamp() ON CREATE set cn.host={host}, cn.createTs=timestamp(),cn.updateTs=timestamp(),cn.primary=false ";
		neo4j.execCypher(cypher, "id", getLocalProcessId(), "host", getLocalHost());

		long now = System.currentTimeMillis();
		AtomicReference<ObjectNode> thisNode = new AtomicReference<ObjectNode>(null);
		List<ObjectNode> list = List.class.cast(neo4j.execCypherAsList("match (cn:ClusterNode) return cn"));

		list.stream().forEach(p -> {
			long timeSinceLastUpdate = now - p.path("updateTs").asLong(0);
			p.put("timeSinceLastUpdate", timeSinceLastUpdate);
			p.put("online", timeSinceLastUpdate < TimeUnit.SECONDS.toMillis(scanIntervalSecs) * 3);
			boolean isPrimary = p.path("primary").asBoolean(false);

			p.put("primary", isPrimary);

			if (isThisNode(p)) {
				thisNode.set(p);
				p.put("self", true);
			} else {
				p.put("self", false);
			}

		});

		List<ObjectNode> removeList = Lists.newArrayList();

		list.forEach(p -> {
			if (!p.path("online").asBoolean(false)) {
				removeList.add(p);
			}
		});

		removeList.forEach(it -> {
			list.remove(it);
			expireNode(it);
		});

		Collections.sort(list, clusterNodeComparator());
		int primaryCount = list.stream().mapToInt(it -> {
			return it.path("primary").asBoolean() ? 1 : 0;
		}).sum();

		if (logger.isDebugEnabled()) {
			logger.debug("primary count: {}", primaryCount);
		}

		if (primaryCount > 1) {
			forceElection(thisNode.get());
		} else if (primaryCount == 0) {
			if (isThisNode(list.get(0))) {
				announceIntentionToBecomePrimary();
			}
		}

		Map<String, NodeInfo> map = Maps.newHashMap();
		list.forEach(it -> {
			map.put(it.path("id").asText(), new NodeInfo(it));
		});

		clusterNodeMapRef.set(ImmutableMap.copyOf(map));

		handleLocalStateChange(thisNode.get());

		if (logger.isDebugEnabled()) {
			logger.debug("cluster nodes: {}", getClusterNodes().values());
		}

	}

	public Map<String, NodeInfo> getClusterNodes() {
		return clusterNodeMapRef.get();
	}

	protected void forceElection(ObjectNode self) {
		String cypher = "match (cn:ClusterNode) where cn.primary=true set cn.primary=false";
		neo4j.execCypher(cypher);

		if (self != null) {
			// We are transitioning all nodes to secondary to force an election
			// set the value here so that handleLocalStateChange() catches this
			// properly
			self.put("primary", false);
		}

	}

	protected void handleLocalStateChange(ObjectNode newState) {
		if (newState == null) {
			return;
		}
		boolean isNewStatePrimary = newState.path("primary").asBoolean(false);

		if (primaryStatus.get() == isNewStatePrimary) {
			if (logger.isDebugEnabled()) {
				logger.debug("no state change");
			}
		} else {
			if (isNewStatePrimary) {

				primaryStatus.set(true);
				onStepUp();
			} else {

				primaryStatus.set(false);
				onStepDown();
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("this node is {}", primaryStatus.get() ? "PRIMARY" : "SECONDARY");
		}
	}

	protected void onStepUp() {
		logger.info("this node is {}", primaryStatus.get() ? "PRIMARY" : "SECONDARY");
		logger.info("cluster: {}", getClusterNodes().values());
	}

	protected void onStepDown() {
		logger.info("this node is {}", primaryStatus.get() ? "PRIMARY" : "SECONDARY");
		logger.info("cluster: {}", getClusterNodes().values());
	}

	boolean isThisNode(JsonNode n) {
		return n.path("id").asText().equals(getLocalProcessId());
	}

	protected void announceIntentionToBecomePrimary() {
		logger.info("announcing intention to become primary");
		String cypher = "match (cn:ClusterNode {id:{id}}) set cn.primary=true";
		neo4j.execCypher(cypher, "id", getLocalProcessId());
	}

	protected void expireNode(JsonNode n) {
		String cypher = "match (cn:ClusterNode {id:{id}}) delete cn";

		logger.info("removing cluster nade: {}", n);
		neo4j.execCypher(cypher, "id", n.get("id").asText());
	}

	public static Comparator<JsonNode> clusterNodeComparator() {
		Ordering<JsonNode> priority = new Ordering<JsonNode>() {

			@Override
			public int compare(JsonNode left, JsonNode right) {

				return -1 * Ints.compare(left.path("priority").asInt(0), right.path("priority").asInt(0));
			}

		};
		Ordering<JsonNode> order = new Ordering<JsonNode>() {

			@Override
			public int compare(JsonNode left, JsonNode right) {

				return left.path("id").asText("").compareTo(right.path("id").asText(""));
			}
		};

		Comparator<JsonNode> x = Ordering.compound(ImmutableList.of(priority, order));
		return x;
	}

	protected void ensureUniqueConstraints() {
		try {
			neo4j.execCypher("CREATE CONSTRAINT ON (cn:ClusterNode) ASSERT cn.id IS UNIQUE");
		} catch (Exception e) {
			logger.error("problem creating unique constraint");
		}
	}

	public boolean isEligibleAsLeader() {
		return leaderEligibilitySupplierRef.get().get();
	}

	public void setLeaderEligibilitySupplier(Supplier<Boolean> supplier) {
		com.google.common.base.Preconditions.checkNotNull(supplier);
		this.leaderEligibilitySupplierRef.set(supplier);
	}

	/**
	 * Convenience for allowing other systems to query leader state.
	 * @return
	 */
	public Supplier<Boolean> getLeaderStateSupplier() {
		return new Supplier<Boolean>() {

			@Override
			public Boolean get() {
				return isLeader();
			}
		};
	}

	/**
	 * Convenience for allowing other subsystems to query leader state.
	 * @return
	 */
	public com.google.common.base.Supplier<Boolean> getLeaderStateGuavaSupplier() {
		return new com.google.common.base.Supplier<Boolean>() {

			@Override
			public Boolean get() {
				return isLeader();
			}
		};
	}
}
