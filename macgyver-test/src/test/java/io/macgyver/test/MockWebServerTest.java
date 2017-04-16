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
package io.macgyver.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;

import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * This class is less of a test than a demonstration of MockWebServer.
 *
 */
public class MockWebServerTest {

	Logger logger = LoggerFactory.getLogger(MockWebServerTest.class);

	@Rule
	public final MockWebServer mockServer = new MockWebServer();

	List<Integer> portList = new ArrayList<Integer>();

	@Test
	public void demonstrateFirst() throws Exception {
		portList.add(mockServer.getPort());
		assertThat(mockServer).isNotNull();
		assertThat(mockServer.url("/test").toString()).isEqualTo(
				"http://localhost:" + mockServer.getPort() + "/test");

		// Confirm behavior of MockServerRule, a new port per test
		if (portList.size() == 2) {
			assertThat(portList.get(0)).isNotEqualTo(portList.get(1));
		}
	}

	@Test
	public void demonstrateSecond() throws Exception {

		portList.add(mockServer.getPort());
		assertThat(mockServer).isNotNull();
		assertThat(mockServer.url("/test").toString()).isEqualTo(
				"http://localhost:" + mockServer.getPort() + "/test");

		// Confirm behavior of MockServerRule, a new port per test
		if (portList.size() == 2) {
			assertThat(portList.get(0)).isNotEqualTo(portList.get(1));
		}
	}

	@Test
	public void testIt() throws IOException, InterruptedException {

		// set up mock response
		mockServer.enqueue(new MockResponse().setBody("{\"name\":\"Rob\"}")
				.addHeader("Content-type", "application/json"));
		
		// set up client and request
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder()
				.url(mockServer.url("/test").toString())
				.post(RequestBody.create(MediaType.parse("application/json"),
						"{}"))
				.header("Authorization", Credentials.basic("scott", "tiger"))
				.build();

		// make the call
		Response response = client.newCall(request).execute();

		// check the response
		assertThat(response.code()).isEqualTo(200);
		assertThat(response.header("content-type")).isEqualTo(
				"application/json");
		
		// check the response body
		JsonNode n = new ObjectMapper().readTree(response.body().string());
		assertThat(n.path("name").asText()).isEqualTo("Rob");
		
		// now make sure that the request was as we exepected it to be
		RecordedRequest recordedRequest = mockServer.takeRequest();
		assertThat(recordedRequest.getPath()).isEqualTo("/test");
		assertThat(recordedRequest.getHeader("Authorization")).isEqualTo("Basic c2NvdHQ6dGlnZXI=");
		
	}

	@Test
	public void testGetWithUrlConnection() throws IOException,
			InterruptedException {

		mockServer.enqueue(new MockResponse().setBody("hello"));

		// Now connect to the mock server and get the greeting
		URL url = mockServer.url("/greet").url();

		HttpURLConnection c = (HttpURLConnection) url.openConnection();

		c.connect();

		// assert that we got what we expected
		assertThat(c.getResponseCode()).isEqualTo(200);
		assertThat(
				CharStreams.toString(new InputStreamReader(c.getInputStream(),
						"UTF-8"))).isEqualTo("hello");

		// we should have made a single request
		assertThat(mockServer.getRequestCount()).isEqualTo(1);

		RecordedRequest request = mockServer.takeRequest();
		assertThat(request.getPath()).isEqualTo("/greet");
		assertThat(request.getMethod()).isEqualTo("GET");

	}
}
