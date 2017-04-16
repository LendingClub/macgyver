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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import io.macgyver.core.service.ServiceDefinition;
import io.macgyver.core.service.ServiceFactory;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

public class ServiceDefinitionTest {

	ServiceFactory<Boolean> testServiceFactory = new ServiceFactory<Boolean>("test") {

		@Override
		protected Boolean doCreateInstance(ServiceDefinition def) {
			// TODO Auto-generated method stub
			return Boolean.TRUE;
		}

	
	};
	
	
	@Test
	public void testCreateCollab() {
		Properties p = new Properties();
		p.put("a", "1");
		ServiceDefinition def = new ServiceDefinition("foo", "foo","testType", p, testServiceFactory);
		
		assertSame(testServiceFactory,def.getServiceFactory());
		
		ServiceDefinition cDef = def.createCollaboratorDefintiion("Bar");
		assertTrue(def.isLazyInit());
		assertEquals("fooBar", cDef.getName());
		assertEquals("foo",cDef.getPrimaryName());
		assertEquals(def.getProperties().getProperty("a"),cDef.getProperties().getProperty("a"));
		assertSame(def.getServiceFactory(),cDef.getServiceFactory());
		
		assertTrue(cDef.isCollaborator());
		assertFalse(def.isCollaborator());
	}
	@Test
	public void testDeepProperties() {
		Properties props = new Properties();
		props.put("a", "1");
		
		
		ServiceDefinition def = new ServiceDefinition("test", "test", "testType",props, testServiceFactory);	
		def.getProperties().put("b", "2");
		Assert.assertEquals("1",def.getProperties().getProperty("a"));
		Assert.assertEquals("2", def.getProperties().getProperty("b"));
		Assert.assertFalse(props.containsKey("b"));
		
		props.put("c", "3");
		Assert.assertFalse(def.getProperties().containsKey("c"));
		
	}
}
