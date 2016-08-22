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

import io.macgyver.core.service.ServiceRegistry;
import io.macgyver.core.util.JsonNodes;
import io.macgyver.neorx.rest.NeoRxClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

import org.ocpsoft.prettytime.PrettyTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;

@Controller
@RequestMapping("/api/cmdb")
public class CmdbApiController {

	Logger logger = LoggerFactory.getLogger(CmdbApiController.class);

	@Autowired
	AppInstanceManager manager;

	@Autowired
	ServiceRegistry registry;

	@Autowired
	AppInstanceManager appInstanceManager;

	ObjectMapper mapper = new ObjectMapper();

	@Value("${SUPPRESS_ACCESS_LOG_ATTRIBUTE:SUPPRESS_ACCESS_LOG}")
	String conditionalLoggingAttribute = "SUPPRESS_ACCESS_LOG";

	@Autowired
	NeoRxClient neo4j;

	Function<HttpServletRequest, Boolean> checkInAuthenticator = new Function<HttpServletRequest, Boolean>() {

		@Override
		public Boolean apply(HttpServletRequest t) {

			return true;
		}

	};

	@RequestMapping(value = "checkIn", method = { RequestMethod.PUT, RequestMethod.POST,
			RequestMethod.GET }, produces = "application/json")
	@PreAuthorize("permitAll")
	public ResponseEntity<ObjectNode> checkIn(

			HttpServletRequest request) throws IOException {

		if (!checkInAuthenticator.apply(request)) {
			return new ResponseEntity<ObjectNode>(HttpStatus.UNAUTHORIZED);
		}

		ObjectNode data = toObjectNode(request);

		if (!Strings.isNullOrEmpty(conditionalLoggingAttribute)) {
			// allow tomcat to be configured to suppress access log
			request.setAttribute(conditionalLoggingAttribute, "true");
		}

		appInstanceManager.processCheckIn(data);

		return new ResponseEntity<ObjectNode>(mapper.createObjectNode(), HttpStatus.OK);
	}

	@RequestMapping(value = { "/appInstances", "/app-instances" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ROLE_MACGYVER_USER','ROLE_MACGYVER_API_RO')")
	public ResponseEntity<List<JsonNode>> allAppInstances() {
		String cypher = "match (x:AppInstance) return x";
		List<JsonNode> results = neo4j.execCypher(cypher).toList().toBlocking().first();
		beautifyTimestamps(results);
		return new ResponseEntity<List<JsonNode>>(results, HttpStatus.OK);
	}

	@RequestMapping(value = { "/appInstances/environment/{env}",
			"/app-instances/environment/{env}" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ROLE_MACGYVER_USER','ROLE_MACGYVER_API_RO')")
	public ResponseEntity<List<JsonNode>> appInstancesByEnv(@PathVariable String env) {
		String cypher = "match (x:AppInstance {environment:{env}}) return x";
		List<JsonNode> results = neo4j.execCypher(cypher, "env", env).toList().toBlocking().first();
		beautifyTimestamps(results);
		return new ResponseEntity<List<JsonNode>>(results, HttpStatus.OK);
	}

	@RequestMapping(value = { "/appInstances/environment/{env}/appId/{appId}",
			"/app-instances/environment/{env}/appId/{appId}" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ROLE_MACGYVER_USER','ROLE_MACGYVER_API_RO')")
	public ResponseEntity<List<JsonNode>> appInstance(@PathVariable String env, @PathVariable String appId) {
		String cypher = "match (x:AppInstance {appId:{appIds},environment:{env}}) return x";
		List<JsonNode> results = neo4j.execCypher(cypher, "appIds", appId, "env", env).toList().toBlocking().first();
		beautifyTimestamps(results);
		return new ResponseEntity<List<JsonNode>>(results, HttpStatus.OK);
	}

	protected void beautifyTimestamps(List<JsonNode> list) {
		PrettyTime pt = new PrettyTime();
		for (JsonNode n : list) {
			long val = n.path("lastContactTs").asLong(0);
			Date d = new Date(val);
			ObjectNode on = (ObjectNode) n;

			on.put("lastContactPrettyTs", pt.format(d));
		}
	}

	public static ObjectNode toObjectNode(HttpServletRequest request) throws IOException {
		ObjectNode data = JsonNodes.mapper.createObjectNode();
		if (request.getMethod().equalsIgnoreCase("GET")) {

			Enumeration<String> t = request.getParameterNames();
			while (t.hasMoreElements()) {
				String key = t.nextElement();
				String val = request.getParameter(key);
				data.put(key, val);
			}
		} else if (request.getMethod().equalsIgnoreCase("PUT") || request.getMethod().equalsIgnoreCase("POST")) {
			if (request.getContentType().contains("json")) {
				try (InputStream is = request.getInputStream()) {
					data = (ObjectNode) JsonNodes.mapper.readTree(is);
				}

			}
		}
		return data;
	}
}
