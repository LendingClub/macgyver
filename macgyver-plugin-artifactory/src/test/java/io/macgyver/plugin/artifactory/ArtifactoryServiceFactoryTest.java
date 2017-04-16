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
package io.macgyver.plugin.artifactory;

import java.io.IOException;
import java.net.ProxySelector;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import io.macgyver.core.service.BasicServiceFactory;
import io.macgyver.core.util.StandaloneServiceBuilder;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class ArtifactoryServiceFactoryTest {

	@Rule
	public MockWebServer mockWebServer = new MockWebServer();

	@Test
	public void testMissingUrl() {
		try {
			StandaloneServiceBuilder.forServiceFactory(ArtifactoryServiceFactory.class).build();
			Assertions.failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
		} catch (IllegalArgumentException e) {
			Assertions.assertThat(e).hasMessage("url must be set");
		}
	}

	@Test
	public void testWithUrl() throws InterruptedException, IOException {

		mockWebServer.enqueue(new MockResponse().setBody("{}").setResponseCode(200));
		ArtifactoryClient c = ArtifactoryClient.class
				.cast(StandaloneServiceBuilder.forServiceFactory(ArtifactoryServiceFactory.class)
						.property("url", mockWebServer.url("/artifactory").toString()).build());

		Assertions.assertThat(c.getBaseTarget().getUrl()).isEqualTo(mockWebServer.url("/artifactory").toString());

		JsonNode x = c.searchGAVC().artifact("foo").execute();

		RecordedRequest rr = mockWebServer.takeRequest();

		Assertions.assertThat(rr.getHeader("Authorization")).isNull();
		Assertions.assertThat(rr.getRequestLine()).startsWith("GET /artifactory/api/search/gavc?a=foo");
	}

	@Test
	public void testWithUrlAndCredentials() throws InterruptedException, IOException {
	
		mockWebServer.enqueue(new MockResponse().setBody("{}").setResponseCode(200));
		ArtifactoryClient c = ArtifactoryClient.class.cast(StandaloneServiceBuilder
				.forServiceFactory(ArtifactoryServiceFactory.class).property("username", "scott")
				.property("password", "tiger").property("url", mockWebServer.url("/artifactory").toString()).build());

		Assertions.assertThat(c.getBaseTarget().getUrl()).isEqualTo(mockWebServer.url("/artifactory").toString());

		JsonNode x = c.searchGAVC().artifact("foo").execute();

		RecordedRequest rr = mockWebServer.takeRequest();

		Assertions.assertThat(rr.getHeader("Authorization")).isEqualTo("Basic c2NvdHQ6dGlnZXI=");
		Assertions.assertThat(rr.getRequestLine()).startsWith("GET /artifactory/api/search/gavc?a=foo");
	}
	

}
