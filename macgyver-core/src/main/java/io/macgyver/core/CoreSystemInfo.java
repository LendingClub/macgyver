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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.macgyver.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.io.Closer;

public class CoreSystemInfo {

	Logger logger = LoggerFactory.getLogger(CoreSystemInfo.class);

	@Autowired
	org.springframework.context.ApplicationContext applicationContext;

	Supplier<Map<String,String>> combinedSupplier = new CombinedSupplier();
	Supplier<Map<String, String>> coreSupplier = Suppliers.memoize(new SystemInfoSupplier());

	Supplier<Map<String, String>> additionalDataSupplier = null;

	
	class CombinedSupplier implements Supplier<Map<String,String>> {

		@Override
		public Map<String, String> get() {
			
			Map<String,String> core = Maps.newHashMap();
			core.putAll(coreSupplier.get());
			
			if (additionalDataSupplier!=null) {
				core.putAll(additionalDataSupplier.get());
			}
			
			return core;
		}
		
	}
	class SystemInfoSupplier implements Supplier<Map<String, String>> {

		@Override
		public Map<String, String> get() {
			
			return ImmutableMap.copyOf(loadRevisionInfo());
		}

	}

	public Map<String, String> getData() {
		return combinedSupplier.get();
	}

	public void setExtraMetadataSupplier(Supplier<Map<String, String>> s) {
		this.additionalDataSupplier = s;
	}

	protected Map<String, String> loadRevisionInfo() {
		Closer c = Closer.create();
		try {

			Resource resource = applicationContext
					.getResource("classpath:macgyver-core-revision.properties");
			if (resource.exists()) {

				InputStream is = resource.getInputStream();
				c.register(is);
				if (is != null) {
					Properties p = new Properties();
					p.load(is);
					Map<String, String> m = Maps.newHashMap();
					for (Map.Entry<Object, Object> entry : p.entrySet()) {
						m.put(entry.getKey().toString(), entry.getValue().toString());
					}
					return m;
				}

			}

		} catch (Exception e) {
			logger.warn("could not load revision info", e);
		} finally {
			try {
				c.close();
			} catch (IOException e) {
				logger.warn("", e);
			}
		}
		return Maps.newHashMap();
	}

}
