/**
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
package io.macgyver.plugin.cmdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;

import io.macgyver.core.event.DistributedEvent;
import io.macgyver.core.event.DistributedEventSystem;
import io.macgyver.core.util.JsonNodes;
import io.macgyver.core.util.Neo4jPropertyFlattener;
import io.macgyver.neorx.rest.NeoRxClient;

public class AppInstanceManager {
	Logger logger = LoggerFactory.getLogger(AppInstanceManager.class);

	@Autowired
	NeoRxClient neo4j;

	@Autowired
	DistributedEventSystem distributedEventSystem;
	
	Neo4jPropertyFlattener flattener = new Neo4jPropertyFlattener();
	
	CheckInProcessor processor = new BasicCheckInProcessor();


	public ObjectNode processCheckIn(ObjectNode data) {

		data = rx.Observable.just(data).map(new Neo4jPropertyFlattener()).toBlocking().first();
	
		String host = data.path("host").asText().replace(".tlcinternal.com","");
		String group = data.path("groupId").asText();
		String app = data.path("appId").asText();
		String index = data.path("index").asText("default");
		
		if (host.toLowerCase().equals("unknown") || host.toLowerCase().equals("localhost")) {
			return new ObjectMapper().createObjectNode();
		} else {
			if (Strings.isNullOrEmpty(group)) {
				group = "";
			}
			logger.debug("host:{} group:{} app:{}", host, group, app);
	
			if (!Strings.isNullOrEmpty(host) && !Strings.isNullOrEmpty(app)) {	
				ObjectNode set = new ObjectMapper().createObjectNode();
				set.setAll(data);
				set.put("lastContactTs", System.currentTimeMillis());
								
				ObjectNode p = new ObjectMapper().createObjectNode();
				p.put("h", host);
				p.put("gi", group);
				p.put("ai", app);
				p.put("q", index);
				set.put("index", index);
				p.set("props", set);
				
				String query = "match (x:AppInstance {host:{h}, appId:{ai}, index:{q}}) return x";
				
				JsonNode current = neo4j.execCypher(query,p).toBlocking().firstOrDefault(null);
				
				
				String cypher = "merge (x:AppInstance {host:{h}, appId:{ai}, index:{q}}) set x={props} return x";
	
				JsonNode r = neo4j.execCypher(cypher, p).toBlocking().firstOrDefault(null);
				processChanges(current, set);
				if (r!=null) {
					return (ObjectNode) r;
				}
			}
			return new ObjectMapper().createObjectNode();
		}
	}
	public CheckInProcessor getCheckInProcessor() {
		return processor;
	}
	public void setCheckInProcessor(CheckInProcessor p) {
		this.processor = p;
	}
	
	boolean hasAttributeChanged(JsonNode a, JsonNode b, String attribute) {
		return a!=null && b!=null && (!a.path(attribute).asText().equals(b.path(attribute).asText()));
	}
	
	public void processChanges(JsonNode currentProperties, JsonNode newProperties) {
		if (currentProperties==null && newProperties!=null) {
			publishChange("app.instance.discover",currentProperties,newProperties);
			publishChange("app.instance.start",currentProperties,newProperties);
		}
	
		if (currentProperties!=null && newProperties!=null) {
			if (hasAttributeChanged(currentProperties, newProperties, "version")) {
				// version change
				publishChange("app.instance.version.update",currentProperties, newProperties);
			}		
			if (hasAttributeChanged(currentProperties,newProperties,"revision")) {
				publishChange("app.instance.revision.update",currentProperties,newProperties);
			}
			if (hasAttributeChanged(currentProperties,newProperties,"processId")) {
				publishChange("app.instance.start",currentProperties,newProperties);
			}
			
		}

	}

	protected void publishChange(String topic, JsonNode currentProperties, JsonNode newProperties) {
		ObjectNode payload = JsonNodes.mapper.createObjectNode();
		payload.set("previous", currentProperties);
		payload.set("current",newProperties);
		DistributedEvent evt = DistributedEvent.create().topic(topic).payload(payload);
		
		logger.info("change: "+JsonNodes.pretty(evt.getJson()));
		distributedEventSystem.publish(evt);
		
	}
}
