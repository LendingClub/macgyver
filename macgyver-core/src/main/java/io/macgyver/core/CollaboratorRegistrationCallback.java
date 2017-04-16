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

import java.util.Properties;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

public abstract class CollaboratorRegistrationCallback {

	public abstract void registerCollaborators(RegistrationDetail detail);
	
	public static class RegistrationDetail {

		BeanDefinitionRegistry registry;
		Properties properties;
		BeanDefinition primaryBeanDefinition;
		String primaryBeanName;
		public BeanDefinitionRegistry getRegistry() {
			return registry;
		}
		public void setRegistry(BeanDefinitionRegistry registry) {
			this.registry = registry;
		}
		public Properties getProperties() {
			return properties;
		}
		public void setProperties(Properties properties) {
			this.properties = properties;
		}
		public BeanDefinition getPrimaryBeanDefinition() {
			return primaryBeanDefinition;
		}
		public void setPrimaryBeanDefinition(BeanDefinition primaryBeanDefinition) {
			this.primaryBeanDefinition = primaryBeanDefinition;
		}
		public String getPrimaryBeanName() {
			return primaryBeanName;
		}
		public void setPrimaryBeanName(String primaryBeanName) {
			this.primaryBeanName = primaryBeanName;
		}

	}
}
