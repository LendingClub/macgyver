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
package io.macgyver.cli;

import java.io.File;
import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Files;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.macgyver.cli.CLI;

public class ConfigManagerTest {

	ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void test() throws IOException{
		CLI cli = new CLI();
		
		File configDir = Files.newTemporaryFolder();
		File configFile = new File(configDir,"config");
		Assertions.assertThat(configFile).doesNotExist();
		cli.getConfigManager().setConfigDir(configDir);
	
		Assertions.assertThat(cli.getConfigManager().getConfigFile().getAbsolutePath()).isEqualTo(configFile.getAbsolutePath());
	
		cli.getConfigManager().loadConfig();
		ObjectNode n = cli.getConfigManager().getData();
		System.out.println(n);
		Assertions.assertThat(n.size()).isEqualTo(0);
		
		n.put("foo", "bar");
		cli.getConfigManager().saveConfig();
		
		ObjectNode n2 = (ObjectNode)  mapper.readTree(configFile);
		
		Assertions.assertThat(n2.path("foo").asText()).isEqualTo("bar");
		
	}
}
