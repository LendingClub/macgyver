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
package io.macgyver.core.auth;

import java.util.Optional;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.lendingclub.neorx.NeoRxClient;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.macgyver.core.util.JsonNodes;

@Controller
@PreAuthorize("hasAnyRole('ROLE_MACGYVER_USER', 'ROLE_MACGYVER_ADMIN')")
@RequestMapping("/api/core/token")
public class ApiTokenController {

	@Inject
	NeoRxClient neo4j;

	@Inject
	ApiTokenAuthenticationProvider provider;

	ObjectMapper mapper = new ObjectMapper();

	@RequestMapping(value = "/create", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public String create(HttpServletRequest request) {

		return JsonNodes.pretty(provider.createToken());

	}

	@RequestMapping(value = "/validate", method = { RequestMethod.GET })
	@ResponseBody
	public String validate(HttpServletRequest request) {
		return SecurityContextHolder.getContext().getAuthentication().toString();
	}

	@RequestMapping(value = "/refresh", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public String refresh(HttpServletRequest request) {
		ApiToken existingToken = ApiToken.parse(ApiTokenAuthenticationFilter.extractTokenString(request).get());

		JsonNode n = provider.refreshToken(existingToken);

		return JsonNodes.pretty(n);
	}

	@RequestMapping(value = "/delete-all", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public JsonNode deleteAll(HttpServletRequest request) {

		String username = SecurityContextHolder.getContext().getAuthentication().getName();

		String cypher = "match (a:ApiToken {username:{username}}) detach delete a";

		// there is a deficiency here where we really only want to be able to
		// delete our own token. Fix this later.
		neo4j.execCypher(cypher, "username", username);

		ObjectNode n = mapper.createObjectNode().put("status", "deleted");

		return n;

	}

	@RequestMapping(value = "/delete", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<JsonNode> delete(HttpServletRequest request) {

		Optional<String> token = ApiTokenAuthenticationFilter.extractTokenString(request);
		if (token.isPresent()) {
			String cypher = "match (a:ApiToken {accessKey:{accessKey}}) detach delete a";

			// there is a deficiency here where we really only want to be able
			// to delete our own token. Fix this later.
			neo4j.execCypher(cypher, "accessKey", ApiToken.parse(token.get()).getAccessKey());

			ObjectNode n = mapper.createObjectNode().put("status", "deleted");

			return ResponseEntity.ok(n);
		
		}
		else {
			return ResponseEntity.status(403).body(mapper.createObjectNode());
		}
	}
}
