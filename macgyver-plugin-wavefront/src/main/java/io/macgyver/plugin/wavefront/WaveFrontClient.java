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
package io.macgyver.plugin.wavefront;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.macgyver.core.service.ProxyConfigManager.ProxyConfig;
import io.macgyver.okrest3.OkRestClient;
import io.macgyver.okrest3.OkRestTarget;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.RequestBody;

public class WaveFrontClient {

	public static final String DEFAULT_ENDPOINT_URL = "https://try.wavefront.com/api";

	ObjectMapper mapper = new ObjectMapper();

	OkRestTarget restTarget;

	public class ApiKeyInterceptor implements Interceptor {

		String apiKey;

		public ApiKeyInterceptor(String key) {
			this.apiKey = key;
		}

		@Override
		public okhttp3.Response intercept(Chain chain) throws IOException {

			okhttp3.Request request = chain.request().newBuilder().addHeader("X-WF-CSRF-TOKEN", apiKey).build();
			return chain.proceed(request);
		}

	}

	public static class Builder {

		String url = DEFAULT_ENDPOINT_URL;

		String apiKey;
		ProxyConfig proxyConfig;

		public Builder apiKey(String apiKey) {
			this.apiKey = apiKey;
			return this;
		}

		public Builder withProxyConfig(ProxyConfig pc) {
			this.proxyConfig = pc;
			return this;
		}

		public Builder url(String url) {
			this.url = url;
			return this;
		}

		public WaveFrontClient build() {

			WaveFrontClient client = new WaveFrontClient();

			OkRestClient.Builder b = new OkRestClient.Builder().withInterceptor(client.new ApiKeyInterceptor(apiKey));

			if (proxyConfig != null) {
				proxyConfig.apply(b);
			}
			client.restTarget = b.build().uri(url);
			return client;

		}
	}

	public OkRestTarget getRestTarget() {
		return restTarget.path("v2");
	}

	public ObjectNode get(String x, String... args) {

		OkRestTarget localTarget = getRestTarget().path(x);

		localTarget = localTarget.queryParam((Object[]) args)
				.accept(org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
				.contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE);

		return localTarget.get().execute(ObjectNode.class);

	}

}
