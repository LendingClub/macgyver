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
package io.macgyver.plugin.newrelic;

import io.macgyver.core.service.BasicServiceFactory;
import io.macgyver.core.service.ServiceDefinition;

public class NewRelicServiceFactory extends BasicServiceFactory<NewRelicClient>{

	public NewRelicServiceFactory() {
		super("NewRelic");
		
	}

	@Override
	protected NewRelicClient doCreateInstance(ServiceDefinition def) {
		
		
		
		
		NewRelicClient c = new NewRelicClient.Builder().withProxyConfig(def.getProxyConfig().orElse(null)).url(def.getProperties().getProperty("url", NewRelicClient.DEFAULT_ENDPOINT_URL)).apiKey(def.getProperties().getProperty("apiKey")).build();
		
		return c;
	}



}
