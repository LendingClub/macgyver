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
package io.macgyver.cli.command;

import java.io.File;
import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Files;
import org.junit.Test;

import com.beust.jcommander.ParameterException;

import io.macgyver.cli.CLI;

public class HelpCommandTest {
	
	@Test
	public void helpTest() throws IOException {
		CLI cli = new CLI();

		File configDir = Files.newTemporaryFolder();

		cli.getConfigManager().setConfigDir(configDir);

		try {
			cli.run("config", "--help");
		} catch (Exception e) {
			Assertions.fail("Failed to run the help command.");
		}
		
	}
	
	@Test
	public void helpTestNegative() throws IOException {
		CLI cli = new CLI();

		File configDir = Files.newTemporaryFolder();

		cli.getConfigManager().setConfigDir(configDir);
		try {
			cli.run("config", "--test");
		} catch (Exception e) {
			Assertions.assertThat(e).isInstanceOf(ParameterException.class)
			.hasMessage("Unknown option: --test");
		}
		
	}
}
