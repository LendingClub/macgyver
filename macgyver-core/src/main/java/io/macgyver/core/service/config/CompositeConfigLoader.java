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
package io.macgyver.core.service.config;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

import io.macgyver.core.crypto.Crypto;

public class CompositeConfigLoader extends ConfigLoader {

	Logger logger = LoggerFactory.getLogger(CompositeConfigLoader.class);
	List<ConfigLoader> loaders = Lists.newCopyOnWriteArrayList();
	
	@Autowired
	Crypto crypto;
	

	public void addLoader(ConfigLoader cl) {
		loaders.add(cl);
	}

	List<ConfigLoader> getConfigLoaders() {
		return loaders;
	}
	@Override
	public void applyConfig(Map<String, String> m) {
		
		Map<String,String> data = m;
		for (Function<Map<String,String>,Map<String,String>> f: loaders) {
			
			logger.info("applying {}",f);
			data = f.apply(data);
			
			
		}

		logger.info("decrypting properties...");
		Properties p = new Properties();
		p.putAll(m);
		p = crypto.decryptProperties(p);
		
		p.entrySet().forEach(it -> {
			m.put(it.getKey().toString(), it.getValue().toString());
		});
		logger.info("property decryption complete.");
	}
	
	
}
