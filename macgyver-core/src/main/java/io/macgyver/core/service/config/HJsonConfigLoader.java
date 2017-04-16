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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.internal.Maps;
import com.fasterxml.jackson.databind.JsonNode;

import io.macgyver.core.Bootstrap;
import io.macgyver.core.Kernel;
import io.macgyver.core.MacGyverConfigurationException;
import io.macgyver.core.util.HJson;
import io.macgyver.core.util.MacGyverObjectMapper;

public class HJsonConfigLoader extends ConfigLoader {

	Logger logger = LoggerFactory.getLogger(HJsonConfigLoader.class);

	@Override
	public void applyConfig(Map<String, String> m) {

		File servicesJson = Bootstrap.getInstance().resolveConfig("services.hjson");

		if (servicesJson.exists()) {
			try {
				logger.info("resource found: {}", servicesJson);
				JsonNode n = HJson.parse(servicesJson);
				m.putAll(process(n, Kernel.getExecutionProfile().orNull()));
			} catch (IOException e) {
				throw new MacGyverConfigurationException(e);
			}
		} else {
			logger.info("resource not found: {}", servicesJson);
		}

		servicesJson = Bootstrap.getInstance().resolveConfig("services.json");
		if (servicesJson.exists()) {
			try {
				logger.info("resource found: {}", servicesJson);
				JsonNode n = MacGyverObjectMapper.objectMapper.readTree(servicesJson);
				m.putAll(process(n, Kernel.getExecutionProfile().orNull()));
			} catch (IOException e) {
				throw new MacGyverConfigurationException(e);
			}
		} else {
			logger.info("resource not found: {}", servicesJson);
		}

	}

	public Map<String, String> process(JsonNode n, String env) {
		Map<String, String> m = Maps.newHashMap();

		n.fields().forEachRemaining(it -> {
			if ((!it.getKey().equals("environments"))) {

				if (it.getValue().isObject()) {
					it.getValue().fields().forEachRemaining(x -> {

						m.put(it.getKey() + "." + x.getKey(), x.getValue().asText());
					});
				} else {
					logger.warn("unknown config: {}={}", it.getKey(), it.getValue());
				}

			}

		});

		JsonNode holder = n.path("environments").path(env);
		if (env != null && holder.isMissingNode()) {
			logger.warn("environment not found in config: {}", env);
		} else {
			holder.fields().forEachRemaining(it -> {
				if (it.getValue().isObject()) {
					it.getValue().fields().forEachRemaining(x -> {

						m.put(it.getKey() + "." + x.getKey(), x.getValue().asText());
					});
				} else {
					logger.warn("unknown config: {}={}", it.getKey(), it.getValue());
				}
			});
		}
		return m;
	}
}
