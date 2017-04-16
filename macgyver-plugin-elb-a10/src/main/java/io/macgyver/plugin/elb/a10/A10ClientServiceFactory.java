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
package io.macgyver.plugin.elb.a10;

import io.macgyver.core.service.ServiceDefinition;
import io.macgyver.core.service.ServiceFactory;

public class A10ClientServiceFactory extends ServiceFactory<A10Client> {

	public A10ClientServiceFactory() {
		super("a10");

	}

	@Override
	protected A10Client doCreateInstance(ServiceDefinition def) {

		boolean certVerificationEnabled = Boolean.parseBoolean(def
				.getProperties().getProperty(CERTIFICATE_VERIFICATION_ENABLED,
						"false"));

		A10HAClientImpl c = new A10HAClientImpl(def.getProperties()
				.getProperty("url"), def.getProperties()
				.getProperty("username"), def.getProperties().getProperty(
				"password"), A10HAClientImpl.DEFAULT_NODE_CHECK_SECS,
				certVerificationEnabled);

		return c;
	}

}
