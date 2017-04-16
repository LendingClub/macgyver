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
package io.macgyver.mongodb;

import io.macgyver.core.service.ServiceRegistry;
import io.macgyver.test.MacGyverIntegrationTest;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class MongoDBFactoryBeanTest extends MacGyverIntegrationTest {

	@Autowired
	ServiceRegistry reg;

	@Test(expected = IllegalArgumentException.class)
	public void testCredentialInjectorIllegalArgument() {
		MongoDBServiceFactory cf = new MongoDBServiceFactory();

		cf.injectCredentials("something", "abc", "def");
	}

	@Test()
	public void testCredentialInjectorNoCredentials() {
		MongoDBServiceFactory cf = new MongoDBServiceFactory();

		Assert.assertEquals("mongodb://host/db",
				cf.injectCredentials("mongodb://host/db", null, null));
		Assert.assertEquals("mongodb://host/db",
				cf.injectCredentials("mongodb://host/db", "", ""));
	}

	@Test()
	public void testCredentialInjectorWIthCredentials() {
		MongoDBServiceFactory cf = new MongoDBServiceFactory();

		Assert.assertEquals("mongodb://scott:tiger@host/db",
				cf.injectCredentials("mongodb://host/db", "scott", "tiger"));

	}

	@Test
	public void testConfig() {
		Assert.assertNotNull(reg.get("testMongo"));

		Assert.assertNotNull(reg.get("testMongoDB"));
	}

}
