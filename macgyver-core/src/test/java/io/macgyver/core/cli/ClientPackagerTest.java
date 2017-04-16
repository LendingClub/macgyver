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
package io.macgyver.core.cli;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import io.macgyver.core.cli.ClientPackager;

public class ClientPackagerTest {

	@Test
	public void testIt() throws IOException, net.lingala.zip4j.exception.ZipException{
		
		ClientPackager cp = new ClientPackager();
		
		cp.getMacGyvverCLIExecutable().delete();
		
		Assertions.assertThat(cp.getMacGyvverCLIExecutable()).doesNotExist();
		
		File f = cp.rebuild();
		
		Assertions.assertThat(f).exists();
		Assertions.assertThat(f.getAbsolutePath()).contains(".cli-generated");
	}
}
