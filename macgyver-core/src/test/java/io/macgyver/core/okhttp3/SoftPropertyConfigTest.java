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
package io.macgyver.core.okhttp3;

import java.util.Properties;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;

import io.macgyver.core.okhttp3.SoftPropertyConfig;
import io.macgyver.okrest3.BasicAuthInterceptor;
import io.macgyver.okrest3.OkRestClient;
import io.macgyver.okrest3.OkRestClient.Builder;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class SoftPropertyConfigTest {

	@Rule
	public MockWebServer mockServer = new MockWebServer();
	
	@Test
	public void testIntVal() {
		Assertions.assertThat(SoftPropertyConfig.safeIntValue(new Properties(), "foo").isPresent())
				.isFalse();
		Assertions.assertThat(SoftPropertyConfig.safeIntValue(new Properties(), "").isPresent()).isFalse();
		Assertions.assertThat(SoftPropertyConfig.safeIntValue(new Properties(), null).isPresent())
				.isFalse();
		Assertions.assertThat(SoftPropertyConfig.safeIntValue(null, "foo").isPresent()).isFalse();

		Properties p = new Properties();
		p.put("foo", "abc");

		Assertions.assertThat(SoftPropertyConfig.safeIntValue(p, "foo").isPresent()).isFalse();

		p.put("foo", "123");

		Assertions.assertThat(SoftPropertyConfig.safeIntValue(p, "foo").get()).isEqualTo(123);

		p.put("foo", " 456 ");

		Assertions.assertThat(SoftPropertyConfig.safeIntValue(p, "foo").get()).isEqualTo(456);

		p.put("foo", " -456 ");

		Assertions.assertThat(SoftPropertyConfig.safeIntValue(p, "foo").get()).isEqualTo(-456);

		p.put("foo", " 456.44 ");

		Assertions.assertThat(SoftPropertyConfig.safeIntValue(p, "foo").isPresent()).isFalse();
	}
	
	@Test
	public void testBasicAuthInterceptor() throws InterruptedException{
		
		
		Properties p = new Properties();
		p.put("username", "scott");
		p.put("password", "tiger");
	
		OkRestClient c = new OkRestClient.Builder().withOkHttpClientConfig(SoftPropertyConfig.basicAuthConfig(p)).build();
		
		c.getOkHttpClient().interceptors().forEach(interceptor -> {
			Assertions.assertThat(interceptor).isInstanceOf(BasicAuthInterceptor.class);
		});
		
		mockServer.enqueue(new MockResponse().setBody("bar"));
		String x = c.uri(mockServer.url("/foo").toString()).get().execute(String.class);
		
		RecordedRequest rr = mockServer.takeRequest();
		
		Assertions.assertThat(rr.getHeader("Authorization")).isEqualTo(Credentials.basic("scott", "tiger"));
	}
}
