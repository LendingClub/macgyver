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
package io.macgyver.plugin.newrelic;

import java.io.IOException;

import io.macgyver.core.ServiceInvocationException;
import io.macgyver.core.util.JsonNodes;
import io.macgyver.okrest.OkRestException;
import io.macgyver.test.InternetAccess;
import io.macgyver.test.MacGyverIntegrationTest;
import okhttp3.FormBody;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import org.aspectj.lang.annotation.Before;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;

import java.net.InetAddress;
public class NewRelicClientTest  {

	Logger logger = LoggerFactory.getLogger(NewRelicClientTest.class);

	@Rule
	public MockWebServer mockServer = new MockWebServer();


	@Test
	public void testX() throws IOException, InterruptedException {
		String x = "      {\"applications\":[  {\n" + 
				"            \"id\": 5189666,\n" + 
				"            \"name\": \"foo-app\",\n" + 
				"            \"language\": \"java\",\n" + 
				"            \"health_status\": \"green\",\n" + 
				"            \"reporting\": true,\n" + 
				"            \"last_reported_at\": \"2016-03-20T23:56:53+00:00\",\n" + 
				"            \"application_summary\": {\n" + 
				"                \"response_time\": 24.7,\n" + 
				"                \"throughput\": 1730.0,\n" + 
				"                \"error_rate\": 0.0,\n" + 
				"                \"apdex_target\": 0.5,\n" + 
				"                \"apdex_score\": 1.0,\n" + 
				"                \"host_count\": 1,\n" + 
				"                \"instance_count\": 1\n" + 
				"            },\n" + 
				"            \"settings\": {\n" + 
				"                \"app_apdex_threshold\": 0.5,\n" + 
				"                \"end_user_apdex_threshold\": 7.0,\n" + 
				"                \"enable_real_user_monitoring\": true,\n" + 
				"                \"use_server_side_config\": false\n" + 
				"            },\n" + 
				"            \"links\": {\n" + 
				"                \"application_instances\": [\n" + 
				"                    5189555\n" + 
				"                ],\n" + 
				"                \"alert_policy\": 111111,\n" + 
				"                \"servers\": [\n" + 
				"                    5193333\n" + 
				"                ],\n" + 
				"                \"application_hosts\": [\n" + 
				"                    3733333\n" + 
				"                ]\n" + 
				"            }\n" + 
				"        }]}";
		
		mockServer.enqueue(new MockResponse().setBody(x));
		NewRelicClient c = new NewRelicClient.Builder().url(mockServer.url("").toString()).apiKey("ffffffaaaaaaaa").build();
		

		
		JsonNode response = c.get("applications","a","b");
		logger.info("response: "+response);

		Assert.assertTrue(response.has("applications"));

RecordedRequest rr = mockServer.takeRequest();
		
		Assertions.assertThat(rr.getRequestLine()).startsWith("GET /v2/applications?a=b");
	}

	@Test
	public void testInvalidKey() {

		try {
			NewRelicClient c = new NewRelicClient.Builder().apiKey("foobar").build();

			JsonNode x = c.get("applications.json");

			Assert.fail("expected exception");
		} catch (io.macgyver.okrest3.OkRestException e) {
			Assertions.assertThat(e).isInstanceOf(
					io.macgyver.okrest3.OkRestException.class);
			Assertions.assertThat(e.getErrorResponseBody()).contains("The API key provided is invalid");
		}
	}
	
	

}
