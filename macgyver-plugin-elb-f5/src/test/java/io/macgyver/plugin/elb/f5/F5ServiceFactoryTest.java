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

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.macgyver.core.LoggingConfig;
import io.macgyver.core.util.StandaloneServiceBuilder;
import okhttp3.Credentials;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class F5ServiceFactoryTest {

	ObjectMapper mapper = new ObjectMapper();

	@Rule
	public MockWebServer mockServer = new MockWebServer();

	F5Client f5;
	
	@BeforeClass
	public static void bridgeLogging() {
		LoggingConfig.ensureJavaUtilLoggingIsBridged();
	}

	@Before
	public void setupTestClient() {

		// Do not call it a mock client. It is a real client and a mock server!

		// instantiate a test client that will communicate with our mock server

		f5 = StandaloneServiceBuilder.forServiceFactory(F5ClientServiceFactory.class)
				.property("url", mockServer.url("/grateful/dead").toString()).property("username", "ChinaCatSunflower")
				.property("password", "IKnowYouRider").build(F5Client.class);
		

		
	}

	@Test
	public void testBasics() throws IOException, InterruptedException {
		
		// not testing anything F5-specific  just that things are set up properly
		mockServer.enqueue(new okhttp3.mockwebserver.MockResponse().setBody("{}"));

		Assertions.assertThat(f5).isNotNull();
		
		f5.getTarget().path("/song").get().execute();
		
		RecordedRequest rr = mockServer.takeRequest();
		
		Assertions.assertThat(rr.getRequestLine()).startsWith("GET /grateful/dead/song HTTP/1.1");
		Assertions.assertThat(rr.getHeader("Authorization")).isEqualTo(Credentials.basic("ChinaCatSunflower", "IKnowYouRider"));

	}

}
