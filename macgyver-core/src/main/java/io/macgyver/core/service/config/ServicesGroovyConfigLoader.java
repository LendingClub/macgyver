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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import groovy.util.ConfigObject;
import groovy.util.ConfigSlurper;
import io.macgyver.core.Bootstrap;
import io.macgyver.core.Kernel;
import io.macgyver.core.MacGyverConfigurationException;

public class ServicesGroovyConfigLoader extends ConfigLoader {

	Logger logger = LoggerFactory.getLogger(ServicesGroovyConfigLoader.class);

	@Override
	public void applyConfig(Map<String, String> m) {

		File configGroovy = Bootstrap.getInstance().resolveConfig("services.groovy");

		logger.info("loading services from: {}", configGroovy);
		ConfigSlurper slurper = new ConfigSlurper();
		if (Kernel.getExecutionProfile().isPresent()) {
			slurper.setEnvironment(Kernel.getExecutionProfile().get());
		}

		if (configGroovy.exists()) {
			try {
				ConfigObject obj = slurper.parse(configGroovy.toURI().toURL());

				Properties p = obj.toProperties();

				p.entrySet().forEach(it -> {
					String key = Objects.toString(it.getKey());
					String val = Objects.toString(it.getValue());
					m.put(key, val);
				});
			} catch (MalformedURLException e) {
				throw new MacGyverConfigurationException(e);
			}

		} else {
			logger.warn("services config file not found: {}", configGroovy);
		}

	}

}
