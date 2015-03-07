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
package io.macgyver.plugin.atlassian.jira;

import static org.assertj.core.api.Assertions.assertThat;
import io.macgyver.core.test.StandaloneServiceBuilder;
import io.macgyver.test.MockServerTest;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockserver.mock.Expectation;
import org.mockserver.mock.action.ExpectationCallback;
import org.mockserver.model.HttpCallback;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.security.Credentials;
import com.squareup.okhttp.Authenticator;

public class JiraClientTest extends MockServerTest {

	Logger logger = LoggerFactory.getLogger(JiraClientTest.class);

	public static class TestCallback implements ExpectationCallback {
		@Override
		public HttpResponse handle(HttpRequest httpRequest) {

			Assertions.assertThat(true).isFalse();
			return HttpResponse.response("{}");
		}
	}

	@Test
	public void testPostIssue() throws Exception {

		getMockServerClient().reset(); // clear out all expectations

		// respond with a dummy response
		getMockServerClient().when(
				HttpRequest.request().withPath("/rest/issue")).respond(
				HttpResponse.response("{}"));

		
		
		// create a jira client that points to our mock server
		JiraClient client = StandaloneServiceBuilder
				.forServiceFactory(JiraServiceFactory.class)
				.property("url", getMockServerUrl() + "/rest").property("username", "JerryGarcia").property("password", "Ripple")
				.build(JiraClient.class);

		JsonNode body = new ObjectMapper().createObjectNode().put("hello",
				"world");
		client.postJson("issue", body);

		
		// Now we can go back and make sure that what we sent matches what we think we should have sent
		Expectation exp = getMockServerClient().retrieveAsExpectations(null)[0];

		
		// Make sure we sent heaers appropriately
		assertThat(exp.getHttpRequest().getFirstHeader("Authorization")).contains("Basic SmVycnlHYXJjaWE6UmlwcGxl");
		assertThat(exp.getHttpRequest().getFirstHeader("Content-Type")).contains("application/json");
		
		// Now look at the body
		JsonNode n = new ObjectMapper().readTree(exp.getHttpRequest()
				.getBodyAsString());
		assertThat(n.get("hello").asText()).isEqualTo("world");

	}
}
