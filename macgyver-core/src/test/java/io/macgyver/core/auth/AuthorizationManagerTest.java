package io.macgyver.core.auth;

import java.util.Collection;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.macgyver.core.auth.AuthorizationVoter.AuthorizationResult;
import io.macgyver.core.util.JsonNodes;

public class AuthorizationManagerTest {

	@Test
	public void testIt() {
		AuthorizationManager authorizationManager = new AuthorizationManager();
		
		
		ObjectNode n = JsonNodes.createObjectNode();
		
		
		Assertions.assertThat(authorizationManager.authorize(n)).isFalse();
		
		
		AuthorizationVoter v = new AuthorizationVoter() {

			@Override
			public AuthorizationResult authorize(JsonNode data) {
				return data.path("username").asText().equals("homer") ? AuthorizationResult.PERMIT : AuthorizationResult.ABSTAIN;
			}
			
		};

		authorizationManager.add(v);
		Assertions.assertThat(authorizationManager.authorize(n)).isFalse();
		n.put("username", "homer");
		Assertions.assertThat(authorizationManager.authorize(n)).isTrue();
		
		
		System.out.println(authorizationManager.createAuthRequest("deploy", "foo"));
		
		
		authorizationManager.authorize("deploy", "app");
	}
	
	
	@Test
	public void testIt2() {
		AuthorizationManager authorizationManager = new AuthorizationManager();
		
		SimpleAuthorizationVoter v = new SimpleAuthorizationVoter() {
			
			@Override
			public AuthorizationResult authorize(String subjectName, Collection<String> roles, String action, JsonNode object) {
				if (object.asText().equals("bar")) {
					return AuthorizationResult.PERMIT;
				}
				return AuthorizationResult.ABSTAIN;
			}
		};
		authorizationManager.add(v);
	
		// We can "foo a bar", but should not be able to "foo a buzz"
		Assertions.assertThat(authorizationManager.authorize("foo","bar")).isTrue();
		Assertions.assertThat(authorizationManager.authorize("foo","buzz")).isFalse();
	}

}
