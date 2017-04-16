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
package io.macgyver.jython;

import io.macgyver.test.MacGyverIntegrationTest;

import javax.script.ScriptEngineManager;

import org.junit.Assert;
import org.junit.Test;

public class ScriptEngineManagerTest extends MacGyverIntegrationTest {

	
	
	@Test
	public void testEngine() {
		
		// There is a defect in jython that prevents this from working 
		// unless Options.importSite=false.  A hack-y fix is to set this in the core module.
		// So that we don't break here.  This is why this is set as an integration test.
		
		ScriptEngineManager m = new ScriptEngineManager();
	
		Assert.assertNotNull(m.getEngineByExtension("py"));
		Assert.assertNotNull(m.getEngineByName("python"));
		
	}
}
