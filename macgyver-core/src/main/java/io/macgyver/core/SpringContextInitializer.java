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
package io.macgyver.core;

import io.macgyver.core.crypto.Crypto;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * This class allows us to access and mutate the environment before the
 * ApplicationContext is booted. This allows us to apply property decryption so
 * that cipher-text values are stored on disk, but plain-text values are
 * injected into the Spring runtime.
 * 
 * @author rschoening
 *
 */
public class SpringContextInitializer implements
		ApplicationContextInitializer<ConfigurableApplicationContext> {

	Logger logger = LoggerFactory.getLogger(SpringContextInitializer.class);

	Crypto crypto = new Crypto();

	Properties capturedProperties = new Properties();
	
	Set<String> interestingProperties = Sets.newHashSet("macgyver.home","neo4j.url","neo4j.username","neo4j.password");
	
	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {

		// We will store any properties that we want to change in this
		// Properties instance
		Properties overrides = new Properties();

		
		
		MutablePropertySources sources = applicationContext.getEnvironment()
				.getPropertySources();

		// convert the Iterator to a List so that we don't encounter
		// ConcurrentModificationException
		// if we want to add/remove PropertySource instances

		List<PropertySource<?>> propertySourceList = Lists.newArrayList(sources
				.iterator());

		for (PropertySource<?> ps : propertySourceList) {

			logger.info("PropertySource: " + ps);

			// We're only interested in EnumerablePropertySource instances,
			// since they are the only ones that we can iterate across keys
			if (ps instanceof EnumerablePropertySource<?>) {

				EnumerablePropertySource<?> eps = (EnumerablePropertySource<?>) ps;

				String propertyNames[] = eps.getPropertyNames();
				if (propertyNames != null) {
					for (String name : propertyNames) {
						Object val = eps.getProperty(name);
						processProperty(name, val, overrides);
					}
				}

			}
		}
		overrides.setProperty("spring.mustache.check-template-location","false");
		overrides.setProperty("spring.gsp.reloadingEnabled", "true");
		overrides.setProperty("spring.gsp.templateRoots","file:./web/templates,file:./src/main/resources/web/templates,classpath:/web/templates");
		
		// Now that we have accumulated some overrides, we mutate the environment
		if (!overrides.isEmpty()) {
			sources.addFirst(new PropertiesPropertySource("macgyver-override",
					overrides));
		}
		
		
		capturedProperties.put("macgyver.home", applicationContext.getEnvironment().getProperty("macgyver.home"));
		Bootstrap.getInstance().init(capturedProperties);
	
		

	}

	protected void processProperty(String name, Object val, Properties overrides) {
		logger.debug("processProperty: {}", name);
		if (val != null && val instanceof String) {
			
			
			
			// apply decryption
			String decryptedValue = crypto.decryptStringWithPassThrough(val
					.toString());
			if (!val.equals(decryptedValue)) {
				overrides.put(name, val);
			}
			
			if (interestingProperties.contains(name)) {
				capturedProperties.put(name, val.toString());
			}
		}
	}

}
