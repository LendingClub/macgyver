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

import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;



import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.macgyver.core.okhttp3.SoftPropertyConfig;
import io.macgyver.core.service.ServiceDefinition;
import io.macgyver.okrest3.OkHttpClientConfigurer;
import io.macgyver.okrest3.OkRestClient;

public class ArtifactoryServiceFactory extends io.macgyver.core.service.ServiceFactory<ArtifactoryClient> {

	public ArtifactoryServiceFactory() {
		super("artifactory");

	}

	@Override
	protected ArtifactoryClient doCreateInstance(ServiceDefinition def) {

		Properties props = def.getProperties();

		String url = props.getProperty("url");

		Preconditions.checkArgument(!Strings.isNullOrEmpty(url), "url must be set");

		Properties vals = new Properties();
		vals.put("readTimeout", Integer.toString(ArtifactoryClient.READ_TIMEOUT_DEFAULT));
		vals.put("connectTimeout", Integer.toString(ArtifactoryClient.CONNECT_TIMEOUT_DEFAULT));
		vals.put("writeTimeout", Integer.toString(ArtifactoryClient.WRITE_TIMEOUT_DEFAULT));

		vals.putAll(props);

		ArtifactoryClient c = new ArtifactoryClient.Builder().url(url)
				.withOkHttpClientConfig(SoftPropertyConfig.timeoutConfig(vals))
				.withOkHttpClientConfig(SoftPropertyConfig.certificateVerificationConfig(vals))
				.withOkHttpClientConfig(SoftPropertyConfig.basicAuthConfig(vals))

				.build();

		return c;
	}

}
