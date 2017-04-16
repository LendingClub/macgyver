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
package io.macgyver.plugin.etcd;

import java.net.URI;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import io.macgyver.core.service.ServiceDefinition;
import mousio.etcd4j.EtcdClient;

public class EtcdClientServiceFactory extends
		io.macgyver.core.service.ServiceFactory<EtcdClient> {

	public EtcdClientServiceFactory() {
		super("etcd");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Object doCreateInstance(ServiceDefinition def) {

		Properties p = def.getProperties();

		String uriList = p.getProperty("uri", "http://127.0.0.1:4001");

		List<String> x = Splitter.on(Pattern.compile("[;,\\s]"))
				.omitEmptyStrings().splitToList(uriList);

		logger.info("etcd uri list: {}",x);
		List<URI> tmp = Lists.newArrayList();
		for (String uri : x) {
			try {

				URI u = URI.create(uri);
				tmp.add(u);
			} catch (Exception e) {
				logger.warn("problem parsing uri", e);
			}
		}

		EtcdClient c = new EtcdClient(tmp.toArray(new URI[0]));
		return c;
	}

}
