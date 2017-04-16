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
package io.macgyver.core.web.mvc;

import java.io.File;
import java.net.MalformedURLException;

import io.macgyver.core.Bootstrap;
import io.macgyver.core.MacGyverConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

public class MacGyverWeb extends WebMvcConfigurerAdapter {

	Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {

		try {
			String webDir = new File(Bootstrap.getInstance().getWebDir(),
					"resources").getAbsoluteFile().toURI().toURL().toString();
			String classpathLocation = "classpath:/web/resources/";
			logger.info("static resources served from: {} and {}", webDir,
					classpathLocation);
			registry.addResourceHandler("/resources/**").addResourceLocations(
					webDir, classpathLocation);
		} catch (MalformedURLException e) {
			throw new MacGyverConfigurationException(e);
		}
	}

}
