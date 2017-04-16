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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TestCLI extends CLI {

	public static class MemoryConfigManager extends ConfigManager {

	
		@Override
		public ObjectNode getData() {
			return data;
		}

		@Override
		public void setConfigDir(File f) {

		}

		@Override
		public File getConfigDir() {
			throw new UnsupportedOperationException();
		}

		@Override
		public File getConfigFile() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void loadConfig() throws IOException {

		}

		@Override
		public void saveConfig() throws IOException {

		}

	}

	public TestCLI() {
		super(new MemoryConfigManager());
	
		
	}
	
	public TestCLI withConfig(String key, String val) {

		getConfigManager().getData().put(key, val);
	
		
		return this;
	}

}
