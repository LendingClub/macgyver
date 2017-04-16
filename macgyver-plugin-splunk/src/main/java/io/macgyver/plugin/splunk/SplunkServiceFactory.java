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
package io.macgyver.plugin.splunk;

import io.macgyver.core.service.BasicServiceFactory;
import io.macgyver.core.service.ServiceDefinition;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.WeakHashMap;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.splunk.HttpService;
import com.splunk.SSLSecurityProtocol;
import com.splunk.Service;

public class SplunkServiceFactory extends BasicServiceFactory<Service> {

	@Autowired
	TokenRefreshManager tokenRefresh;

	public SplunkServiceFactory() {
		super("Splunk");

	}

	@Override
	protected Service doCreateInstance(ServiceDefinition def) {

		Properties p = def.getProperties();

		Map<String, Object> args = Maps.newHashMap();

		for (Map.Entry<Object, Object> entry : p.entrySet()) {
			args.put(entry.getKey().toString(), entry.getValue());
		}

		Object portObject = args.get("port");
		if (portObject != null) {
			args.put("port", Integer.parseInt(portObject.toString()));
		}

		String sslSecurityProtocol = p.getProperty("sslSecurityProtocol");
		if (!Strings.isNullOrEmpty(sslSecurityProtocol)) {
			// Set globally for the splunk client.  Wrong, but little we can do about it.
			HttpService.setSslSecurityProtocol(SSLSecurityProtocol
					.valueOf(sslSecurityProtocol));
		}

		Service service = Service.connect(args);

		tokenRefresh.registerService(service);

		return service;

	}

}
