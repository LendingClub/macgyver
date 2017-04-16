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
package io.macgyver.plugin.hipchat;

import java.util.Optional;

import io.macgyver.core.service.ProxyConfigManager.ProxyConfig;
import io.macgyver.core.service.ServiceDefinition;

public class HipChatServiceFactory extends
		io.macgyver.core.service.ServiceFactory<HipChatClientImpl> {

	public HipChatServiceFactory() {
		super("hipchat");

	}

	@Override
	protected HipChatClientImpl doCreateInstance(ServiceDefinition def) {
		
		String token = def.getProperties().getProperty("token", "");
		String url = def.getProperties().getProperty("url","https://api.hipchat.com");
		
		
		Optional<ProxyConfig> proxyConfig = def.getProxyConfig();
		
		
		HipChatClientImpl c = new HipChatClientImpl(url, token,proxyConfig.orElse(null));
		
		
		return c;
	}


}
