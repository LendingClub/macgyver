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
package io.macgyver.core.auth;

import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Lists;

public abstract class SimpleAuthorizationVoter implements AuthorizationVoter {

	String actionFilter;

	public SimpleAuthorizationVoter() {
		this(null);
	}
	
	public SimpleAuthorizationVoter(String action) {
		this.actionFilter = action;
	}
	
	@Override
	public final AuthorizationResult authorize(JsonNode data) {
		String requestAction = data.path("action").asText();
		
		// This is just an optimization.  If the actionFilter doesn't match, don't bother running any more logic.
		if (actionFilter !=null && !requestAction.equals(actionFilter)) {
			return AuthorizationResult.ABSTAIN;
		}
		
		String subjectName = data.path("subject").path("name").asText();
		
		JsonNode roles = data.path("subject").path("roles");
		List<String> rolesList = Lists.newArrayList();
		if (roles!=null && roles.isArray()) {
			
			ArrayNode an = (ArrayNode) roles;
			an.forEach(it -> {
				rolesList.add(an.asText());
			});
		}
		
		JsonNode object = data.path("object");
		
		return authorize(subjectName,rolesList,requestAction,object);
	}
	
	public abstract AuthorizationResult authorize(String subjectName, Collection<String> roles, String action, JsonNode object);

}
