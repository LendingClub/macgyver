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
package io.macgyver.plugin.elb.f5;

import static org.assertj.core.api.Assertions.assertThat;
import io.macgyver.core.LoggingConfig;
import io.macgyver.plugin.elb.f5.F5RemoteException;
import io.macgyver.test.RequestUtil;
import okhttp3.ConnectionSpec;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class F5ClientTest {

	ObjectMapper mapper = new ObjectMapper();

	@Rule
	public MockWebServer mockServer = new MockWebServer();

	public F5Client testClient = null;

	@BeforeClass
	public static void bridgeLogging() {
		LoggingConfig.ensureJavaUtilLoggingIsBridged();
	}

	@Before
	public void setupTestClient() {

		// Do not call it a mock client. It is a real client and a mock server!

		// instantiate a test client that will communicate with our mock server
		testClient = new F5Client.Builder().withUrl(mockServer.url("/foo/").toString())
				.withCredentials("myusername", "mypassword").build();

	}

	@Test
	public void testIt() throws IOException, InterruptedException {
		mockServer.enqueue(new okhttp3.mockwebserver.MockResponse().setBody("{}"));

		testClient.getTarget().path("/bar/baz").post("{\"foo\":\"bar\"}").execute(JsonNode.class);

		RecordedRequest rr = mockServer.takeRequest();

		Assertions.assertThat(rr.getHeader("Authorization")).isEqualTo("Basic bXl1c2VybmFtZTpteXBhc3N3b3Jk");
		Assertions.assertThat(rr.getRequestLine()).startsWith("POST /foo/bar/baz HTTP");

	}



}
