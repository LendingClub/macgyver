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
package io.macgyver.plugin.atlassian.jira;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.macgyver.core.util.StandaloneServiceBuilder;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class JiraClientTest   {

	Logger logger = LoggerFactory.getLogger(JiraClientTest.class);

	@Rule
	public final MockWebServer mockServer = new MockWebServer();

	
	/**
	 * This is not a test of the actual JIRA REST API.  But it does test that the payload and auth headers
	 * are intact.
	 * @throws Exception
	 */
	@Test
	public void testPost() throws Exception {

	
		
		mockServer.enqueue(new MockResponse().setBody("{\"foo\":\"bar\"}"));
	

		// create a jira client that points to our mock server
		JiraClient client = io.macgyver.core.util.StandaloneServiceBuilder
				.forServiceFactory(JiraServiceFactory.class)
				.property("url", mockServer.url("/rest").toString())
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

	assertThat(recordedRequest.getPath()).isEqualTo("/rest/issue");

	}
	
	/**
	 * This is not a test of the actual JIRA REST API.  
	 */
	@Test
	public void testGetIssue() throws Exception {

	
		
		mockServer.enqueue(new MockResponse().setBody("{\"foo\":\"bar\"}"));
		JiraClientImpl c = new JiraClientImpl(mockServer.url("/rest").toString(), "username", "password");
		
		JsonNode n = c.getIssue("JIRA-999");
		
		RecordedRequest rr = mockServer.takeRequest();
		
		Assertions.assertThat(n).isNotNull();
		Assertions.assertThat(rr.getPath()).isEqualTo("/rest/issue/JIRA-999");
		
	}
	/**
	 * This is not a test of the actual JIRA REST API.  But it does test that the payload and auth headers
	 * are intact.
	 * @throws Exception
	 */
	@Test
	public void testPostPath() throws Exception {

	
		
		mockServer.enqueue(new MockResponse().setBody("{\"foo\":\"bar\"}"));
	

		// create a jira client that points to our mock server
		JiraClient client = StandaloneServiceBuilder
				.forServiceFactory(JiraServiceFactory.class)
				.property("url", mockServer.url("/rest").toString())
				.property("username", "JerryGarcia")
				.property("password", "Ripple").build(JiraClient.class);

		JsonNode body = new ObjectMapper().createObjectNode().put("hello",
				"world");
		JsonNode response = client.postJson("issue/JIRA-123", body);

		assertThat(response.path("foo").asText()).isEqualTo("bar");
		
		// Now we can go back and make sure that what we sent matches what we
		// think we should have sent
		
		RecordedRequest recordedRequest = mockServer.takeRequest();
		
	
		// Make sure we sent heaers appropriately
		assertThat(recordedRequest.getHeader("authorization"))
				.contains("Basic SmVycnlHYXJjaWE6UmlwcGxl");  // basic auth for JerryGarcia/Ripple
		assertThat(recordedRequest.getHeader("Content-Type"))
				.contains("application/json");

		assertThat(recordedRequest.getPath()).isEqualTo("/rest/issue/JIRA-123");
	

	}
	
	@Test
	public void testGetClient() {
		JiraClient client = StandaloneServiceBuilder
				.forServiceFactory(JiraServiceFactory.class)
				.property("url", mockServer.url("/rest").toString())
				.property("username", "JerryGarcia")
				.property("password", "Ripple").build(JiraClient.class);
		
		Assertions.assertThat(client.getOkRestTarget()).isNotNull();
		Assertions.assertThat(client.getOkRestTarget().getOkRestClient()).isNotNull();
		Assertions.assertThat(client.getOkRestTarget().getOkRestClient().getOkHttpClient()).isNotNull();
	}
}
