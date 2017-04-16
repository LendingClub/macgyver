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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import io.macgyver.test.MacGyverIntegrationTest;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class BootstrapTest extends MacGyverIntegrationTest {

	@Autowired 
	ApplicationContext ctx;
	
	@Test
	public void testPaths() throws IOException {

		Bootstrap bootstrap = Bootstrap.getInstance();
		assertNotNull(bootstrap);
		assertSame(bootstrap, Bootstrap.getInstance());
		
		File d = new File(".").getCanonicalFile();
		assertEquals(d,Bootstrap.getInstance().getMacGyverHome().getCanonicalFile());
		assertEquals(new java.io.File("./config").getCanonicalPath(),Bootstrap.getInstance().getConfigDir().getCanonicalPath());
		assertEquals(new java.io.File("./web").getCanonicalPath(),Bootstrap.getInstance().getWebDir().getCanonicalPath());
		assertEquals(new java.io.File("./scripts").getCanonicalPath(),Bootstrap.getInstance().getScriptsDir().getCanonicalPath());
		
	}
	
	

}
