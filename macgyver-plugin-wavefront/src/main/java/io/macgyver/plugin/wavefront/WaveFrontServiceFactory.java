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

import io.macgyver.core.service.BasicServiceFactory;
import io.macgyver.core.service.ServiceDefinition;

public class WaveFrontServiceFactory extends BasicServiceFactory<WaveFrontClient> {

	public WaveFrontServiceFactory() {
		super("WaveFront");
	}

	@Override
	protected WaveFrontClient doCreateInstance(ServiceDefinition def) {

		WaveFrontClient c = new WaveFrontClient.Builder().withProxyConfig(def.getProxyConfig().orElse(null))
				.url(def.getProperties().getProperty("url", WaveFrontClient.DEFAULT_ENDPOINT_URL))
				.apiKey(def.getProperties().getProperty("apiKey")).build();

		return c;
	}

}
