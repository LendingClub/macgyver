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

import io.macgyver.test.MacGyverIntegrationTest;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class CheckBeanNamingConvention extends MacGyverIntegrationTest {

	Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	org.springframework.context.ApplicationContext ctx;

	@Test
	public void testNaming() {
		Map<String, Object> beans = ctx.getBeansOfType(Object.class);

		for (Map.Entry<String, Object> entry : beans.entrySet()) {

			@SuppressWarnings("rawtypes")
			Class c = entry.getValue().getClass();
			
			if (c.getPackage().getName().startsWith("io.macgyver")) {
				check(entry.getKey(), c);
			
			}

		}
	}
	
	@SuppressWarnings("rawtypes")
	public void check(String name, Class clazz) {
		if (name.contains("Config") || name.equals("testGroovyBean")) {
			
		}
		else if (!name.startsWith("mac")) {
			Assert.fail("bean naming violation: "+name+" for "+clazz);
		}
		else if (name.toLowerCase().startsWith("macgyver")) {
			Assert.fail("bean naming violation: "+name+" for "+clazz);
		}
	}
}
