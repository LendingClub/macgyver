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
package io.macgyver.plugin.wavefront;

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
public class WaveFrontClientTest  {

	Logger logger = LoggerFactory.getLogger(WaveFrontClientTest.class);

	@Rule
	public MockWebServer mockServer = new MockWebServer();

	@Test
	public void testX() throws IOException, InterruptedException {
		// For real: curl -X POST --header "X-AUTH-TOKEN: xxxx"
		// -d "n=test-wavefront-from-macgyver&h=application%3A%20macgyver-plugin&h=revision%3A%20testrev&l=info&t=test"
		// "https://try.wavefront.com/api/events"
		String x =	"	{\"name\":\"test-wavefront-from-macgyver\",\n" +
				"	\"startTime\":1488325620332,\n" +
				"	\"annotations\":{\"severity\":\"info\",\"type\":\"test\"},\n" +
				"	\"hosts\":[\"application: macgyver-plugin\",\"revision: testrev\"],\n" +
				"	\"isUserEvent\":true,\n" +
				"	\"table\":\"lendingclub\",\n" +
				"	\"creatorId\":\"dev+lendingclub@wavefront.com\",\n" +
				"	\"updaterId\":\"dev+lendingclub@wavefront.com\",\n" +
				"	\"createdAt\":1488325620342,\n" +
				"	\"updatedAt\":1488325620342\n" +
				"}";

		mockServer.enqueue(new MockResponse().setBody(x));
		WaveFrontClient c = new WaveFrontClient.Builder().url(mockServer.url("").toString()).apiKey("ffffffaaaaaaaa").build();

		JsonNode response = c.get("name","a","b");
		logger.info("response: "+response);

		Assert.assertTrue(response.has("name"));

		RecordedRequest rr = mockServer.takeRequest();

		Assertions.assertThat(rr.getRequestLine()).startsWith("GET /v2/name?a=b");
	}

	@Test
	public void testInvalidKey() {

		try {
			WaveFrontClient c = new WaveFrontClient.Builder().apiKey("foobar").build();

			JsonNode x = c.get("applications.json");

			Assert.fail("expected exception");
		} catch (io.macgyver.okrest3.OkRestException e) {
			Assertions.assertThat(e).isInstanceOf(
					io.macgyver.okrest3.OkRestException.class);
			Assertions.assertThat(e.getErrorResponseBody()).contains("resource cannot be found");
		}
	}

}
