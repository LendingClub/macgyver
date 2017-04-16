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
package io.macgyver.test;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

/**
 * SoftDependencyInitializer exists so that we can avoid a cyclic dependency between
 * macgyver-core and macgyver-test.
 * 
 * @author rschoening
 *
 */
public class SoftDependencyInitializer implements
		ApplicationContextInitializer<ConfigurableApplicationContext> {

	Logger logger = LoggerFactory.getLogger(SoftDependencyInitializer.class);

	public static final String JUNIT_PROFILE="junit_env";

	
	@SuppressWarnings("unchecked")
	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {

		ApplicationContextInitializer<ConfigurableApplicationContext> ci = null;

		try {
			List<String> list = Lists.newArrayList(Arrays.asList(applicationContext.getEnvironment().getActiveProfiles()));
			boolean environmentSpecified = list.stream().anyMatch(it -> it.endsWith("_env"));
			if (!environmentSpecified) {
				list.add(JUNIT_PROFILE);
				String activeProfiles=Joiner.on(",").join(list).toString();
				logger.info("for unit testing, setting spring active profiles: {}",activeProfiles);
				applicationContext.getEnvironment().setActiveProfiles(activeProfiles);
			}
			
			
			logger.info("activeProfiles: {}",Arrays.asList(applicationContext.getEnvironment().getActiveProfiles()));
			Class<?> clazz = Class
					.forName("io.macgyver.core.SpringContextInitializer");
			ci = ((ApplicationContextInitializer<ConfigurableApplicationContext>) clazz
					.newInstance());
		} catch (Exception e) {
			logger.warn("", e);
		}
	
		if (ci != null) {
			ci.initialize(applicationContext);
		}

	}

}
