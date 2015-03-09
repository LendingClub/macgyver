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
import io.macgyver.test.MacGyverIntegrationTest;

import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import com.squareup.okhttp.mockwebserver.rule.MockWebServerRule;

public class JiraClientTest   {

	Logger logger = LoggerFactory.getLogger(JiraClientTest.class);

	@Rule
	public final MockWebServerRule mockServer = new MockWebServerRule();

	
	/**
	 * This is not a test of the actual JIRA REST API.  But it does test that the payload and auth headers
	 * are intact.
	 * @throws Exception
	 */
	@Test
	public void testPost() throws Exception {

	
		
		mockServer.enqueue(new MockResponse().setBody("{\"foo\":\"bar\"}"));
	

		// create a jira client that points to our mock server
		JiraClient client = StandaloneServiceBuilder
				.forServiceFactory(JiraServiceFactory.class)
				.property("url", mockServer.getUrl("/rest").toString())
				.property("username", "JerryGarcia")
				.property("password", "Ripple").build(JiraClient.class);

		JsonNode body = new ObjectMapper().createObjectNode().put("hello",
				"world");
		JsonNode response = client.postJson("issue", body);

		assertThat(response.path("foo").asText()).isEqualTo("bar");
		
		// Now we can go back and make sure that what we sent matches what we
		// think we should have sent
		
		RecordedRequest recordedRequest = mockServer.takeRequest();
		
	
		// Make sure we sent heaers appropriately
		assertThat(recordedRequest.getHeader("authorization"))
				.contains("Basic SmVycnlHYXJjaWE6UmlwcGxl");  // basic auth for JerryGarcia/Ripple
		assertThat(recordedRequest.getHeader("Content-Type"))
				.contains("application/json");

	

	}
}
