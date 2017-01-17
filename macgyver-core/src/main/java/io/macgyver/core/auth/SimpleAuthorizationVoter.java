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
