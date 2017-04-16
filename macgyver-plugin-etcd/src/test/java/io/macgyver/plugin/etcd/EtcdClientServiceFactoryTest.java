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
package io.macgyver.plugin.etcd;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;

import io.macgyver.core.util.StandaloneServiceBuilder;
import mousio.etcd4j.EtcdClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class EtcdClientServiceFactoryTest {


	@Rule
	public okhttp3.mockwebserver.MockWebServer mockServer = new MockWebServer();

	@Test
	public void testIt() throws Exception {


		EtcdClient c= StandaloneServiceBuilder.forServiceFactory(EtcdClientServiceFactory.class).property("uri", mockServer.url("/").toString()).build(EtcdClient.class);
		Assertions.assertThat(mockServer).isNotNull();
		mockServer.enqueue(new MockResponse().setBody("{}"));

		c.getVersion();

		RecordedRequest rr = mockServer.takeRequest();
		Assertions.assertThat(rr.getMethod()).isEqualTo("GET");
		Assertions.assertThat(rr.getPath()).isEqualTo("/version");

		c.close();
	}
}
