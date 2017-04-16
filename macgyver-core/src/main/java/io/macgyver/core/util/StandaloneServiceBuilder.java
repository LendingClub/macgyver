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
package io.macgyver.core.util;

import io.macgyver.core.MacGyverException;
import io.macgyver.core.service.ServiceDefinition;
import io.macgyver.core.service.ServiceFactory;
import io.macgyver.core.service.ServiceRegistry;

import java.util.Properties;
import java.util.UUID;

/**
 * StandaloneServiceBuilder allows services to be constructed without the entire
 * MacGyver runtime. Its primary purpose is to facilitate non-integrated/mocked
 * unit testing of services.
 * 
 * @author rschoening
 *
 */
public class StandaloneServiceBuilder {

	ServiceRegistry registry = new ServiceRegistry();

	Class<? extends ServiceFactory> serviceFactoryClass;
	Properties props = new Properties();

	String id;
	ServiceFactory serviceFactory;
	String serviceType=null;
	
	public static StandaloneServiceBuilder forServiceFactory(
			Class<? extends ServiceFactory> clazz) {
		StandaloneServiceBuilder b = new StandaloneServiceBuilder();
		b.serviceFactoryClass = clazz;
		return b;
	}

	public StandaloneServiceBuilder property(String key, String val) {
		props.put(key, val);
		return this;
	}

	public StandaloneServiceBuilder serviceType(String s) {
		this.serviceType = s;
		return this;
	}
	public <T> T build(Class<T> t) {
		return (T) build();
	}
	
	public Object build() {
		doIt();
		return serviceFactory.get(id);
	}

	protected void doIt() {
		try {
			if (id!=null) {
				return;
			}
			id = UUID.randomUUID().toString();

			serviceFactory = serviceFactoryClass.newInstance();

			ServiceDefinition def = new ServiceDefinition(id, id,serviceType, props, serviceFactory);
			registry.addServiceDefinition(def);

			serviceFactory.setServiceRegistry(registry);

			Object x = serviceFactory.get(id);
			

		} catch (IllegalAccessException | InstantiationException e) {
			throw new MacGyverException(e);
		}	
	}
	public Object buildCollaborator(String collab) {
		doIt();
		return serviceFactory.get(id+collab);
	}

		
	
}
