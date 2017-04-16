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
package io.macgyver.plugin.cloud.cloudstack;

import java.util.Properties;

import com.google.common.base.Strings;

import io.macgyver.core.service.ServiceDefinition;
import io.macgyver.core.service.ServiceFactory;

public class CloudStackClientServiceFactory extends
		ServiceFactory<CloudStackClient> {

	public CloudStackClientServiceFactory() {
		super("cloudstack");

	}

	@Override
	protected CloudStackClient doCreateInstance(ServiceDefinition def) {
		Properties p = def.getProperties();

		String url = p.getProperty("url");
		String username = p.getProperty("username");
		String password = p.getProperty("password");

		String accessKey = p.getProperty("accessKey");
		String secretKey = p.getProperty("secretKey");
		CloudStackClientImpl c = new CloudStackClientImpl(url);

		if (!Strings.isNullOrEmpty(username)) {
			c = c.usernamePasswordAuth(username, password);
		} else if (!Strings.isNullOrEmpty("accessKey")) {
			c = c.apiKeyAuth(accessKey, secretKey);
		}

		return c;
	}

}
