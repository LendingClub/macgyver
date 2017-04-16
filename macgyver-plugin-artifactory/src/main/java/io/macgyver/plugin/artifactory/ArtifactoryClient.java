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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import io.macgyver.okrest3.OkHttpClientConfigurer;
import io.macgyver.okrest3.OkRestClient;
import io.macgyver.okrest3.OkRestTarget;




public abstract class ArtifactoryClient {

	public static final int READ_TIMEOUT_DEFAULT=120;
	public static final int CONNECT_TIMEOUT_DEFAULT=20;
	public static final int WRITE_TIMEOUT_DEFAULT=20;
	
	public abstract OkRestTarget getBaseTarget();
	public abstract GAVCSearchBuilder searchGAVC();
	public abstract PropertySearchBuilder searchProperties();
	public abstract DateSearchBuilder searchDate();
	public abstract AQLSearchBuilder searchAQL();
	public abstract File fetchArtifactToDir(String path, File target) throws IOException;
	public abstract File fetchArtifactToFile(String path, File out) throws IOException;
	public abstract File fetchArtifactToTempFile(String path) throws IOException;
	public abstract InputStream fetchArtifact(String path) throws IOException;
	public abstract void delete(String path) throws IOException;
	
	public static class Builder {

		String url;
		String username;
		String password;
		OkRestClient.Builder builder = new OkRestClient.Builder();

		public Builder() {
			this.builder.withOkHttpClientConfig(it -> {
				it.connectTimeout(20, TimeUnit.SECONDS).readTimeout(120, TimeUnit.SECONDS).writeTimeout(30,
						TimeUnit.SECONDS);
			});
		}

		public Builder url(String url) {
			this.url = url;
			return this;
		}

		public Builder withOkHttpClientConfig(OkHttpClientConfigurer c) {
			builder = builder.withOkHttpClientConfig(c);
			return this;
		}

		public Builder credentials(String username, String password) {
			builder.withBasicAuth(username, password);
			return this;

		}

		public Builder configure(Consumer<OkRestClient.Builder> cb) {
			cb.accept(builder);
			return this;
		}

		public ArtifactoryClient build() {

			ArtifactoryClientImpl c = new ArtifactoryClientImpl();

			c.okRestClient = builder.build();

			c.base = c.okRestClient.url(url);

			return c;
		}
	}
}
