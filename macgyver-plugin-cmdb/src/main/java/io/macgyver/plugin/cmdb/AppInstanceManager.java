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
package io.macgyver.plugin.cmdb;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.annotation.PostConstruct;

import org.lendingclub.neorx.NeoRxClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.common.io.ByteStreams;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.macgyver.core.metrics.MetricsUtil;
import io.macgyver.core.util.JsonNodes;
import io.macgyver.core.util.Neo4jPropertyFlattener;
import io.macgyver.plugin.cmdb.AppInstanceMessage.Discovery;
import io.macgyver.plugin.cmdb.AppInstanceMessage.RevisionChange;
import io.macgyver.plugin.cmdb.AppInstanceMessage.StartupComplete;
import io.macgyver.plugin.cmdb.AppInstanceMessage.VersionChange;

public class AppInstanceManager {
	Logger logger = LoggerFactory.getLogger(AppInstanceManager.class);

	@Autowired
	NeoRxClient neo4j;

	List<Function<ObjectNode,ObjectNode>> transformers = new CopyOnWriteArrayList<>();
	

	
	@Autowired
	MetricRegistry metricRegistry;
	
	@Autowired
	io.macgyver.core.event.MacGyverEventPublisher publisher;

	ObjectMapper mapper = new ObjectMapper();

	Neo4jPropertyFlattener flattener = new Neo4jPropertyFlattener();

	Cache<String, String> idCache = CacheBuilder.newBuilder().maximumSize(5000).expireAfterWrite(5, TimeUnit.MINUTES)
			.build();

	Cache<String,String> rateLimitCache = CacheBuilder.newBuilder().maximumSize(500).expireAfterWrite(5, TimeUnit.SECONDS).build();

	BlockingDeque<Runnable> queue;
	volatile ThreadPoolExecutor executor;

	Meter checkInMeter;
	Meter checkInCacheHitMeter;
	
	public AppInstanceManager() {

	}


	private void registerMetrics() {
	
		
		MetricsUtil.monitorExecutor(metricRegistry, executor, "AppInstanceManager");
		
		checkInMeter = metricRegistry.meter(MetricRegistry.name("AppInstanceManager", "checkIn"));
		checkInCacheHitMeter = metricRegistry.meter(MetricRegistry.name("AppInstanceManager", "checkInCacheHit"));
		
	}
	@PostConstruct
	void startIt() {
		
		ThreadFactory tf =  new ThreadFactoryBuilder().setDaemon(true).setNameFormat("AppInstanceManager-%s").build();
		
		queue = new LinkedBlockingDeque<>(500);
		executor = new ThreadPoolExecutor(1,10, 30, TimeUnit.SECONDS, queue,tf,new ThreadPoolExecutor.DiscardOldestPolicy());

		registerMetrics();
		try {

			neo4j.execCypher("CREATE CONSTRAINT ON (a:AppInstance) ASSERT a.id IS UNIQUE");
		}
		catch (RuntimeException e) {
			logger.warn("problem creating unique constraint on AppInstance",e);
		}
	}

	public void exec(Runnable r) {
		if (executor != null) {
			executor.execute(r);
		}
	}

	public void markLastContact(String id) {

		String cypher = "match (a:AppInstance {id:{id}}) set a.lastContactTs=timestamp()";
		Runnable r = new Runnable() {
			public void run() {

				neo4j.execCypher(cypher, "id", id);
			}
		};
		exec(r);
	}

	protected ObjectNode shallowCopyWithValuesOnly(ObjectNode in) {
		ObjectNode n = mapper.createObjectNode();
		in.fields().forEachRemaining(it -> {
			if (it.getValue().isContainerNode()) {

			} else {
				n.set(it.getKey(), it.getValue());
			}
		});
		return n;
	}


	
	public List<Function<ObjectNode, ObjectNode>> getTransformFunctions() {
		return transformers;
	}
	protected ObjectNode transform(ObjectNode data) {
		for (Function<ObjectNode,ObjectNode> fn: transformers) {
			data = fn.apply(data);
		}
		return data;
	}
	public ObjectNode processCheckIn(ObjectNode data) {

		data = transform(data);
		String host = data.path("host").asText();
		String group = data.path("groupId").asText();
		String app = data.path("appId").asText();
		String index = data.path("index").asText("default");

		Optional<String> id = computeId(host, app, index);

		if (!id.isPresent()) {
			return new ObjectMapper().createObjectNode();
		}
		if (host.toLowerCase().equals("unknown") || host.toLowerCase().equals("localhost")) {
			return new ObjectMapper().createObjectNode();
		}
		// look for a signature
		String existingSignature = idCache.getIfPresent(id.get());

		
		checkInMeter.mark();
	
		boolean updateNeo4j = false;
		String currentSignature = computeSignature(data);
		if (existingSignature == null) {
			idCache.put(id.get(), currentSignature);
			updateNeo4j = true;
		} else if (existingSignature.equals(currentSignature)) {
	
			checkInCacheHitMeter.mark();
			markLastContact(id.get());
			return new ObjectMapper().createObjectNode();
		} else {
			updateNeo4j = true;
			idCache.put(id.get(), currentSignature);
		}
		data = shallowCopyWithValuesOnly(data);
		if (Strings.isNullOrEmpty(group)) {
			group = "";
		}

		if (logger.isDebugEnabled()) {
			logger.debug("host:{} group:{} app:{}", host, group, app);
		}
		if (updateNeo4j) {

			ObjectNode set = new ObjectMapper().createObjectNode();
			set.setAll(data);
			set.put("lastContactTs", System.currentTimeMillis());
			set.put("index", index);
			set.put("id", id.get());
			ObjectNode p = new ObjectMapper().createObjectNode();
			p.put("h", host);

			p.put("ai", app);
			p.put("q", index);
			p.put("id", id.get());
			p.set("props", set);

			String query = "match (x:AppInstance {id:{id}}) return x";

			JsonNode existing = neo4j.execCypher(query, "id", id.get()).blockingFirst(null);

			if (existing == null) {
				// there is no value in neo4j

				// delete any old stuff

				Runnable r = new Runnable() {
					public void run() {
						neo4j.execCypher("match (a:AppInstance {host:{h},appId:{ai},index:{q}}) detach delete a", p);

						neo4j.execCypher("merge (a:AppInstance {id:{id}}) set a={props} return a", p);

						idCache.put(id.get(), currentSignature);
						processChanges(null, set);
					}
				};

				if (rateLimitCache.getIfPresent(id.get())==null) {
					rateLimitCache.put(id.get(), "");
					exec(r);
				}

			} else {

				Runnable r = new Runnable() {
					public void run() {
						String cypher = "merge (x:AppInstance {id:{id}}) set x={props} return x";

						JsonNode r = neo4j.execCypher(cypher, p).blockingFirst(null);
				
						processChanges(existing, r);
					}
				};
				
				if (rateLimitCache.getIfPresent(id.get())==null) {
					rateLimitCache.put(id.get(), "");
					exec(r);
				}
		
				
				return new ObjectMapper().createObjectNode();
			}
		}
		return new ObjectMapper().createObjectNode();

	}


	boolean hasAttributeChanged(JsonNode a, JsonNode b, String attribute) {
		return a != null && b != null && (!a.path(attribute).asText().equals(b.path(attribute).asText()));
	}

	public void processChanges(JsonNode currentProperties, JsonNode newProperties) {
		if (currentProperties == null && newProperties != null) {
			publishChange(Discovery.class, currentProperties, newProperties);
			publishChange(StartupComplete.class, currentProperties, newProperties);
		}

		if (currentProperties != null && newProperties != null) {
			if (hasAttributeChanged(currentProperties, newProperties, "version")) {
				// version change
				publishChange(VersionChange.class, currentProperties, newProperties);
			}
			if (hasAttributeChanged(currentProperties, newProperties, "revision")) {
				publishChange(RevisionChange.class, currentProperties, newProperties);
			}
			if (hasAttributeChanged(currentProperties, newProperties, "processId")) {
				publishChange(StartupComplete.class, currentProperties, newProperties);
			}

		}

	}

	protected void publishChange(Class<? extends AppInstanceMessage> topic, JsonNode currentProperties,
			JsonNode newProperties) {
		ObjectNode payload = JsonNodes.mapper.createObjectNode();
		payload.set("previous", currentProperties);
		payload.set("current", newProperties);

		publisher.createMessage().withMessageType(topic).withAttribute("previous", currentProperties)
				.withAttribute("current", newProperties).publish();

	}

	public static Optional<String> computeId(String host, String app, String index) {
		if (Strings.isNullOrEmpty(host)) {
			return Optional.empty();
		}
		if (Strings.isNullOrEmpty(app)) {
			return Optional.empty();
		}
		if (Strings.isNullOrEmpty(index)) {
			index = "default";
		}
		return Optional
				.of(Hashing.sha1().hashString(host + "-" + app + "-" + index, Charset.forName("UTF8")).toString());
	}

	public String computeSignature(ObjectNode n) {
		return computeSignature(n, ImmutableSet.of());
	}

	public String computeSignature(ObjectNode n, Set<String> exclusions) {
		List<String> list = Lists.newArrayList(n.fieldNames());
		Collections.sort(list);

		HashingOutputStream hos = new HashingOutputStream(Hashing.sha1(), ByteStreams.nullOutputStream());

		list.forEach(it -> {

			if (exclusions != null && !exclusions.contains(it)) {
				JsonNode val = n.get(it);
				if (val.isObject() || val.isArray()) {
					// skipping
				} else {
					try {
						hos.write(it.getBytes());
						hos.write(val.toString().getBytes());
					} catch (IOException e) {
					}
				}
			}
		});

		return hos.hash().toString();

	}
	

}
