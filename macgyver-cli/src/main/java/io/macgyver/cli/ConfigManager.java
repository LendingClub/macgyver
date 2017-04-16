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


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ConfigManager {

	ObjectMapper mapper = new ObjectMapper();
	
	ObjectNode data = mapper.createObjectNode();
	
	File configDir = null;
	
	public ObjectNode getData() {
		return data;
	}
	
	public void setConfigDir(File f) {
		configDir=f;
	}
	public File getConfigDir() {
		if (configDir==null) {
			String userHome = System.getProperty("user.home");
			
			File userHomeFile = new File(userHome);
			
			configDir = new File(userHomeFile,".macgyver");	
		}
		if (!configDir.exists()) {
			configDir.mkdirs();
		}
		return configDir;
	}
	
	public File getConfigFile() {
		getConfigDir();
		
		File configFile = new File(getConfigDir(),"config");
		return configFile;
	}
	public void loadConfig() throws IOException{
		
		File configFile = getConfigFile();
		
		if (!configFile.exists()) {
			mapper.writerWithDefaultPrettyPrinter().writeValue(configFile, mapper.createObjectNode());
		}
		data = (ObjectNode) mapper.readTree(configFile);
		
		
	}
	
	public void saveConfig() throws IOException {
		File cfg = getConfigFile();

		mapper.writerWithDefaultPrettyPrinter().writeValue(cfg, getData());
	}
}
