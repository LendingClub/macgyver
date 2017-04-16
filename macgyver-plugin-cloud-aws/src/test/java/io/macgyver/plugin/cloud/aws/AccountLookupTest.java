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
package io.macgyver.plugin.cloud.aws;

import java.util.Optional;
import java.util.Properties;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import io.macgyver.core.ServiceNotFoundException;
import io.macgyver.core.service.ServiceDefinition;
import io.macgyver.core.service.ServiceFactory;
import io.macgyver.core.service.ServiceRegistry;

public class AccountLookupTest {

	@Test
	public void testIt() {

	
		ServiceRegistry registry = new ServiceRegistry();
		
		
		Properties p = new Properties();
		p.put("accountId", "1234567");
	
		ServiceFactory<AWSServiceClient> sf = new AWSServiceFactory();
		sf.setServiceRegistry(registry);
		Assertions.assertThat(sf.getServiceType()).isEqualTo("aws");
		ServiceDefinition def = new ServiceDefinition("myaws", "myaws","aws", p,sf);	
		registry.addServiceDefinition(def);
		
		
		// End of mock setup
		
		Assertions.assertThat(registry.getServiceDefinitions().get("myaws")).isSameAs(def);
		
		AWSServiceClient xdef = registry.get("myaws", AWSServiceClient.class);
		
		
		Assertions.assertThat(xdef.getAccountId()).isEqualTo("1234567");
		
		
		AWSServiceClient x1 = registry.getServiceByProperty("aws", "accountId", "1234567");
		
		Assertions.assertThat(x1).isSameAs(xdef);
		
	
		try {
			registry.getServiceByProperty("aws", "accountId", "1234567");
		}
		catch (ServiceNotFoundException e) {
			Assertions.assertThat(e).isInstanceOf(ServiceNotFoundException.class).hasMessageContaining("could not locat service");
		}
	
		
	}

}
