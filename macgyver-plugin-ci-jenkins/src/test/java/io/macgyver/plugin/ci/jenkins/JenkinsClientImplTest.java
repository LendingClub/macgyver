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
package io.macgyver.plugin.ci.jenkins;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class JenkinsClientImplTest {

	@Rule
	public MockWebServer mockServer = new MockWebServer();

	JenkinsClient client;

	@Before
	public void setupClient() {
		client = new JenkinsClientBuilder()
				.url(mockServer.url("/jenkins").toString())
				.credentials("username", "password").build();

	}

	@Test
	public void testGetBuildQueue() throws InterruptedException {

		mockServer.enqueue(new MockResponse().setBody("{\"items\":[]}"));
		client.getBuildQueue();

		RecordedRequest rr = mockServer.takeRequest();

		Assertions.assertThat(rr.getPath())
				.isEqualTo("/jenkins/queue/api/json");
		Assertions.assertThat(rr.getMethod()).isEqualTo("GET");
	}

	@Test
	public void testOkRestClientGet() throws InterruptedException {
		mockServer
		.enqueue(new MockResponse()
				.setBody("{}"));
		
		client.getOkRestTarget().path("/foo/bar").get().execute();
		
		RecordedRequest rr = mockServer.takeRequest();
		Assertions.assertThat(rr.getRequestLine()).isEqualTo("GET /jenkins/foo/bar HTTP/1.1");
		
	}
	
	@Test
	public void testOkRestClientPost() throws InterruptedException {
		mockServer
		.enqueue(new MockResponse()
				.setBody("{}"));
		
		client.getOkRestTarget().path("/fizz/buzz").post(new ObjectMapper().createObjectNode().put("foo", "bar")).execute();
		
		RecordedRequest rr = mockServer.takeRequest();
		Assertions.assertThat(rr.getRequestLine()).isEqualTo("POST /jenkins/fizz/buzz HTTP/1.1");
		Assertions.assertThat(rr.getHeader("Content-type")).contains("application/json");
		
	}
	@Test
	public void testGetLoadStats() throws InterruptedException {

		mockServer
				.enqueue(new MockResponse()
						.setBody("{\"busyExecutors\":{},\"queueLength\":{},\"totalExecutors\":{},\"totalQueueLength\":{}}"));
		client.getLoadStats();

		RecordedRequest rr = mockServer.takeRequest();

		Assertions.assertThat(rr.getPath()).isEqualTo(
				"/jenkins/overallLoad/api/json");
		Assertions.assertThat(rr.getMethod()).isEqualTo("GET");

	}

	@Test
	public void testGetServerInfo() throws InterruptedException {

		String x = "{\n" + "  \"assignedLabels\" : [ { } ],\n"
				+ "  \"mode\" : \"NORMAL\",\n"
				+ "  \"nodeDescription\" : \"the master Jenkins node\",\n"
				+ "  \"nodeName\" : \"\",\n" + "  \"numExecutors\" : 2,\n"
				+ "  \"description\" : null,\n" + "  \"jobs\" : [ {\n"
				+ "    \"name\" : \" myjob\",\n"
				+ "    \"url\" : \"https://jenkins.example.com/job/myjob/\",\n"
				+ "    \"color\" : \"red\"\n" + "  }]}";
		mockServer.enqueue(new MockResponse().setBody(x));
		client.getServerInfo();

		RecordedRequest rr = mockServer.takeRequest();

		Assertions.assertThat(rr.getPath()).isEqualTo("/jenkins/api/json");
		Assertions.assertThat(rr.getMethod()).isEqualTo("GET");

	}


}
	



