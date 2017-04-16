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
package io.macgyver.ldap;

import io.macgyver.core.service.ServiceRegistry;
import io.macgyver.test.MacGyverIntegrationTest;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

public class LdapFactoryBeanTest extends MacGyverIntegrationTest {

	@Autowired
	ServiceRegistry registry;

	@Test
	public void testLocator() throws ClassNotFoundException {

		Assert.assertEquals(LdapServiceFactory.class, registry
				.getServiceFactory("ldap").getClass());
		Assert.assertEquals(LdapServiceFactory.class, registry
				.getServiceFactory("activedirectory").getClass());
	}

	public void testConfig() {
		LdapContextSource cs = applicationContext.getBean("testLdap",
				LdapContextSource.class);
		Assert.assertNotNull(cs);

		LdapTemplate t = applicationContext.getBean("testLdapTemplate",
				LdapTemplate.class);
		Assert.assertNotNull(t);
		Assert.assertSame(cs, t.getContextSource());

	}
}
