package io.macgyver.core.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public interface AuthorizationVoter {

	public static enum AuthorizationResult {
		PERMIT,
		DENY,
		ABSTAIN;
	};
	
	public AuthorizationResult authorize(JsonNode data);
	
}
