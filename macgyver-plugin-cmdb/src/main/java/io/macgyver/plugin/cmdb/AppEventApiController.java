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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.macgyver.plugin.cmdb;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.lendingclub.neorx.NeoRxClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import io.macgyver.core.event.MacGyverEventPublisher;

@Controller
@PreAuthorize("permitAll")
public class AppEventApiController {

	private static final Pattern IP_PATTERN = Pattern.compile("\\d+\\.\\d+\\.\\d+\\.\\d+");

	Logger logger = LoggerFactory.getLogger(AppEventApiController.class);
	@Autowired
	NeoRxClient neo4j;

	ObjectMapper mapper = new ObjectMapper();

	@Autowired
	MacGyverEventPublisher publisher;

	List<Function<ObjectNode,ObjectNode>> transformFunctions = new CopyOnWriteArrayList<>();
	
	public List<Function<ObjectNode,ObjectNode>> getTransformFunctions() {
		return transformFunctions;
	}
	@RequestMapping(value = "/api/cmdb/app-event", consumes = "application/json", method = RequestMethod.POST)
	public ResponseEntity<JsonNode> receiveAppEvent(@RequestBody JsonNode dx, HttpServletRequest request) {

		ObjectNode data = (ObjectNode) dx;
		
		logger.info("receive app event: {}", data);
		
		for (Function<ObjectNode,ObjectNode> fn: transformFunctions) {
			data = fn.apply(data);
		}
		
		String eventType = data.path("eventType").asText();

		String appId = data.path("appId").asText();

		if (Strings.isNullOrEmpty(eventType)) {
			return ResponseEntity.badRequest().body(mapper.createObjectNode().put("status", "failed"));
		}
		if (Strings.isNullOrEmpty(appId) || "null".equals(appId)) {
			return ResponseEntity.badRequest().body(mapper.createObjectNode().put("status", "failed"));
		}
		String host = com.google.common.base.Splitter.on(".").splitToList(data.path("host").asText().toLowerCase()).get(0);

		ObjectNode props = mapper.createObjectNode();
		String environment = data.path("environment").asText();
		if (!Strings.isNullOrEmpty(environment)) {
			props.put("environment", environment);
		}

		props.put("index", "default");

		if ((!Strings.isNullOrEmpty(host)) && (!Strings.isNullOrEmpty(appId)) && (!Strings.isNullOrEmpty(environment))
				&& !environment.equals("-")) {

			java.util.Optional<String> id = AppInstanceManager.computeId(host, appId, null);

			if (id.isPresent()) {
				String cypher = "merge (a:AppInstance {id:{id}}) set a.lastContactTs=timestamp(), a+={props} return a";

				neo4j.execCypher(cypher, "id", id.get(), "props", props);
			}
		}

		io.macgyver.core.event.MacGyverEventPublisher.MessageBuilder mb = null;
		if (eventType.equals("DEPLOY_INITIATED") || eventType.equals("DEPLOYMENT_INITIATED")) {
			mb = publisher.createMessage(AppInstanceMessage.DeploymentInitiated.class).withMessageBody(data);
		} else if (eventType.equals("DEPLOY_COMPLETE") || eventType.equals("DEPLOYMENT_COMPLETE")) {
			mb = publisher.createMessage(AppInstanceMessage.DeploymentComplete.class).withMessageBody(data);
		} else if (eventType.equals("SHUTDOWN_INITIATED")) {
			mb = publisher.createMessage(AppInstanceMessage.ShutdownInitiated.class).withMessageBody(data);
		} else if (eventType.equals("SHUTDOWN_COMPLETE")) {
			mb = publisher.createMessage(AppInstanceMessage.ShutdownComplete.class).withMessageBody(data);
		} else if (eventType.equals("STARTUP_INITIATED")) {
			mb = publisher.createMessage(AppInstanceMessage.StartupInitiated.class).withMessageBody(data);
		} else if (eventType.equals("STARTUP_COMPLETE")) {
			// The startup complete event is misleading....we have to wait to receive that state from the app
			//mb = publisher.createMessage(AppInstanceMessage.StartupComplete.class).withMessageBody(data);
		}
		if (mb != null) {
			mb.publish();
		}
		return ResponseEntity.ok().body(mapper.createObjectNode().put("status", "ok"));

	}
	public static Function<ObjectNode, ObjectNode> toUnqualifiedHost() {
		Function<ObjectNode, ObjectNode> f = new Function<ObjectNode, ObjectNode>() {

			@Override
			public ObjectNode apply(ObjectNode t) {
				String host = t.path("host").asText();
				if (Strings.isNullOrEmpty(host)) {
					return t;
				}

				List<String> x = Splitter.on(".").splitToList(host);
				if (!x.isEmpty()) {
					if (!isIP(x.get(0))) {
						t.put("host", x.get(0));
					}
				}
				return t;
			}
		};
		return f;

	}
	private static boolean isIP(String x) {
		if (x == null) {
			return false;
		}

		return IP_PATTERN.matcher(x.trim()).matches();

	}
}
