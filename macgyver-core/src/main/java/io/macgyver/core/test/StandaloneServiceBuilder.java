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
package io.macgyver.core.test;

import java.util.Properties;
import java.util.UUID;

import io.macgyver.core.MacGyverException;
import io.macgyver.core.service.ServiceDefinition;
import io.macgyver.core.service.ServiceFactory;
import io.macgyver.core.service.ServiceRegistry;


/**
 * StandaloneServiceBuilder allows services to be constructed without the entire MacGyver runtime.  
 * Its primary purpose is to facilitate non-integrated/mocked unit testing of services.
 * @author rschoening
 *
 */
public class StandaloneServiceBuilder {

	ServiceRegistry registry = new ServiceRegistry();

	Class<? extends ServiceFactory> serviceFactoryClass;
	Properties props = new Properties();

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

	public <T> T build(Class<T> type) {

		try {

			String id = UUID.randomUUID().toString();

			ServiceFactory sf = serviceFactoryClass.newInstance();

			ServiceDefinition def = new ServiceDefinition(id, id, props, sf);
			registry.addServiceDefinition(def);

			sf.setServiceRegistry(registry);

			return (T) sf.get(id);
		} catch (IllegalAccessException | InstantiationException e) {
			throw new MacGyverException(e);
		}
	}
}
