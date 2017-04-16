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
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.Lists;

import io.macgyver.core.auth.AuthorizationVoter.AuthorizationResult;
import io.macgyver.core.util.JsonNodes;

public class AuthorizationManager {

	Logger logger = LoggerFactory.getLogger(AuthorizationManager.class);

	List<AuthorizationVoter> voterList = Lists.newCopyOnWriteArrayList();

	public void add(AuthorizationVoter voter) {
		voterList.add(voter);
	}

	public boolean authorize(String action) {
		return authorize(createAuthRequest(action));
	}

	public boolean authorize(String action, String object) {
		return authorize(createAuthRequest(action, object));
	}

	public boolean authorize(String action, JsonNode object) {
		return authorize(createAuthRequest(action, object));
	}

	public List<AuthorizationVoter> getAuthorizationVoter() {
		return voterList;
	}
	
	public boolean authorize(JsonNode n) {

		int permitCount = 0;
		int denyCount = 0;
		int abstainCount = 0;
		Iterator<AuthorizationVoter> t = voterList.iterator();
		while (t.hasNext()) {
			AuthorizationVoter voter = t.next();
			try {
				AuthorizationResult result = voter.authorize(n);
				if (result == null || result == AuthorizationResult.ABSTAIN) {
					abstainCount++;
				}
				else if (result == AuthorizationResult.PERMIT) {
					logger.info("permit {} by {}",n,voter);
					permitCount++;
				} else if (result == AuthorizationResult.DENY) {
					logger.info("deny {} by {}",n,voter);
					denyCount++;
				} 
				else {
					logger.warn("{} returned unknown value: {}",voter,result);
				}
			} catch (RuntimeException e) {
				logger.warn("authorization voter failed",e);
				abstainCount++;
			}
		}

		if (denyCount > 0 && denyCount > permitCount) {
			logger.info("authz request denied: {}", n);
			return false;
		}
		if (permitCount > 0) {
			logger.info("authz request permitted: {}", n);
			return true;
		}

		logger.info("request not authorized: {}", n);
		return false;
	}

	public ObjectNode createAuthRequest() {

		ObjectNode x = JsonNodes.createObjectNode();

		ObjectNode subject = JsonNodes.createObjectNode();
		x.set("subject", subject);
		org.springframework.security.core.Authentication authentication = SecurityContextHolder
				.getContext().getAuthentication();

		if (authentication != null) {
			subject.put("name", SecurityContextHolder.getContext().getAuthentication().getName());
			Collection<? extends GrantedAuthority> authorities = SecurityContextHolder
					.getContext().getAuthentication().getAuthorities();

			ArrayNode roles = JsonNodes.mapper.createArrayNode();
			subject.set("roles", roles);
			authorities.forEach(it -> {
				String name = it.getAuthority();
				if (name != null) {
					roles.add(name);
				}
			});
		}

		x.set("action", null);
		return x;
	}

	public ObjectNode createAuthRequest(String action, JsonNode item) {
		ObjectNode n = createAuthRequest();
		n.put("action", action);
		n.set("object", item);

		return n;
	}

	public ObjectNode createAuthRequest(String action) {
		String n = null;
		return createAuthRequest(action, n);

	}

	public ObjectNode createAuthRequest(String action, String item) {

		return createAuthRequest(action, TextNode.valueOf(item));

	}

}
