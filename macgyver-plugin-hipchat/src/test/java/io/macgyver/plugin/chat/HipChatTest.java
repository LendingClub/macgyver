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
package io.macgyver.plugin.chat;

import static org.assertj.core.api.Assertions.assertThat;
import io.macgyver.okrest.OkRestException;
import io.macgyver.plugin.hipchat.HipChatClient;
import io.macgyver.plugin.hipchat.HipChatClientImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import com.squareup.okhttp.mockwebserver.MockWebServer;

public class HipChatTest {

	@Rule
	public MockWebServer mockServer = new MockWebServer();

	HipChatClient client;

	@Before
	public void configure() {
		client = new HipChatClientImpl(mockServer.getUrl("/").toString(),
				"dummy");
	}

	@Test
	public void testMultiParams() throws IOException, InterruptedException {

		String simulatedResponse = "{\n"
				+ "    \"items\": [\n"
				+ "        {\n"
				+ "            \"id\": 12345,\n"
				+ "            \"links\": {\n"
				+ "                \"self\": \"https://api.hipchat.com/v2/user/12345\"\n"
				+ "            },\n"
				+ "            \"mention_name\": \"JerryGarcia\",\n"
				+ "            \"name\": \"Jerry Garcia\"\n"
				+ "        }\n"
				+ "    ],\n"
				+ "    \"links\": {\n"
				+ "        \"next\": \"https://api.hipchat.com/v2/user?start-index=1&max-results=1\",\n"
				+ "        \"self\": \"https://api.hipchat.com/v2/user\"\n"
				+ "    },\n" + "    \"maxResults\": 1,\n"
				+ "    \"startIndex\": 0\n" + "}";

		mockServer.enqueue(new MockResponse().setBody(simulatedResponse));

		client.get("user", "max-results","1000", "expand","items");
		RecordedRequest rr = mockServer.takeRequest();

		Assertions.assertThat(rr.getPath()).startsWith("/v2/user?");
		Assertions.assertThat(rr.getPath()).contains("max-results=1000");
		Assertions.assertThat(rr.getPath()).contains("expand=items");

	}

	@Test
	public void testParamsInMap() throws IOException, InterruptedException {
		String simulatedResponse = "{\n"
				+ "    \"items\": [\n"
				+ "        {\n"
				+ "            \"id\": 12345,\n"
				+ "            \"links\": {\n"
				+ "                \"self\": \"https://api.hipchat.com/v2/user/12345\"\n"
				+ "            },\n"
				+ "            \"mention_name\": \"JerryGarcia\",\n"
				+ "            \"name\": \"Jerry Garcia\"\n"
				+ "        }\n"
				+ "    ],\n"
				+ "    \"links\": {\n"
				+ "        \"next\": \"https://api.hipchat.com/v2/user?start-index=1&max-results=1\",\n"
				+ "        \"self\": \"https://api.hipchat.com/v2/user\"\n"
				+ "    },\n" + "    \"maxResults\": 1,\n"
				+ "    \"startIndex\": 0\n" + "}";

		mockServer.enqueue(new MockResponse().setBody(simulatedResponse));

		Map<String, String> params = new HashMap<>();
		params.put("max-results","1000");
		params.put("expand","items");

		client.get("user",params);
		RecordedRequest rr = mockServer.takeRequest();

		assertThat(rr.getPath()).startsWith("/v2/user");

		assertThat(rr.getPath()).contains("max-results=1000");
		assertThat(rr.getPath()).contains("expand=items");
	}


	@Test
	public void testX() throws IOException, InterruptedException {

		String simulatedResponse = "{\n"
				+ "    \"items\": [\n"
				+ "        {\n"
				+ "            \"id\": 12345,\n"
				+ "            \"links\": {\n"
				+ "                \"self\": \"https://api.hipchat.com/v2/user/12345\"\n"
				+ "            },\n"
				+ "            \"mention_name\": \"JerryGarcia\",\n"
				+ "            \"name\": \"Jerry Garcia\"\n"
				+ "        }\n"
				+ "    ],\n"
				+ "    \"links\": {\n"
				+ "        \"next\": \"https://api.hipchat.com/v2/user?start-index=1&max-results=1\",\n"
				+ "        \"self\": \"https://api.hipchat.com/v2/user\"\n"
				+ "    },\n" + "    \"maxResults\": 1,\n"
				+ "    \"startIndex\": 0\n" + "}";

		mockServer.enqueue(new MockResponse().setBody(simulatedResponse));

		JsonNode serverResponse = client.get("user", "max-results", "1");

		RecordedRequest rr = mockServer.takeRequest();

		assertThat(rr.getPath()).isEqualTo("/v2/user?max-results=1");
		assertThat(rr.getHeader("Authorization")).contains("Bearer dummy");

	}

	@Test
	public void testUnauthorized() throws IOException, InterruptedException {

		try {
			String simulatedResponse = "{\n" + "  \"error\": {\n"
					+ "    \"code\": 401,\n"
					+ "    \"message\": \"Invalid OAuth session\",\n"
					+ "    \"type\": \"Unauthorized\"\n" + "  }\n" + "}";

			mockServer.enqueue(new MockResponse().setBody(simulatedResponse)
					.setResponseCode(401));

			client.get("user", "max-results", "1");

		} catch (io.macgyver.okrest3.OkRestException e) {
			Assertions.assertThat(e.getStatusCode()).isEqualTo(401);
		}

	}


}
