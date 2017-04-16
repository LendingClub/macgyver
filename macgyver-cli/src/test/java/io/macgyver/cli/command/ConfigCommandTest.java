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
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Files;
import org.junit.Test;

import com.beust.jcommander.ParameterException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.macgyver.cli.CLI;

public class ConfigCommandTest {

	ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void testSet() throws IOException {
		CLI cli = new CLI();

		File configDir = Files.newTemporaryFolder();

		cli.getConfigManager().setConfigDir(configDir);

		try {
			boolean b = cli.run("config", "--set");
			Assertions.assertThat(b).isFalse();
		} catch (Exception e) {
			Assertions.assertThat(e).isInstanceOf(ParameterException.class)
					.hasMessage("Expected a value after parameter --set");
		}
		String id = UUID.randomUUID().toString();
		try {

			cli.run("config", "--set", "foo", id);
			
		} catch (Exception e) {
			Assertions.assertThat(e).isInstanceOf(ParameterException.class)
					.hasMessage("Expected a value after parameter --set");
		}
		
		File configFile = new File(configDir,"config");
		
		JsonNode n = mapper.readTree(configFile);
		
		System.out.println(n);
	}
}
