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
package io.macgyver.core.resource.provider.filesystem;

import io.macgyver.core.resource.Resource;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class FileSystemResourceLoaderTest {

	@Test
	public void testX() throws IOException {
		
		FileSystemResourceProvider rl = new FileSystemResourceProvider(new File("./src/test/resources/resource-test")) {

			@Override
			public boolean isApprovedPath(File f) {
				return true;
			}
			
		};
		
		boolean foundF1=false;
		boolean foundD1F1=false;
		for (Resource r: rl.findResources()) {
	
			if (r.getPath().equals("f1.txt")) {
				foundF1=true;
			}
			if (r.getPath().equals("d1/d1f1.txt")) {
				foundD1F1=true;
			}
		}
		
		Assert.assertTrue(foundF1);
		Assert.assertTrue(foundD1F1);
	}

}
