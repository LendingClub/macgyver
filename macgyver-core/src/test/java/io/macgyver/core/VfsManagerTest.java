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

import io.macgyver.core.resource.Resource;
import io.macgyver.core.script.ExtensionResourceProvider;
import io.macgyver.test.MacGyverIntegrationTest;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.io.Files;

public class VfsManagerTest extends MacGyverIntegrationTest {


	
	@Autowired
	ExtensionResourceProvider resourceLoader;

	
	@Test
	public void testWebVfs() throws IOException {

		File dir = new File("./web");
		dir.mkdirs();
		File tempFile = new File(dir, ".junit_"
				+ UUID.randomUUID().toString() + ".tmp");
		Files.touch(tempFile);
		try {
			
			File fo = Bootstrap.getInstance().getWebDir();

			Assert.assertTrue(tempFile.exists());

			File vfsTest2 = new File(Bootstrap.getInstance().getWebDir(), tempFile.getName());
			Assert.assertTrue(vfsTest2.exists());
		} finally {
			tempFile.delete();
		}

	}
	
	@Test
	public void testScriptsVfs() throws IOException {

		File dir = new File("./scripts");

		File tempFile = new File(dir, ".junit_"
				+ UUID.randomUUID().toString() + ".tmp");
		Files.touch(tempFile);
		try {

			Resource r = resourceLoader.getResourceByPath("scripts/"+tempFile.getName());
			
			Assert.assertNotNull(r);
			
			Assert.assertTrue(tempFile.exists());

	
		} finally {
			tempFile.delete();
		}

	}
	
	

}
