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

import java.io.IOException;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.macgyver.cli.Command;

public class ConfigCommand extends Command {

	ObjectMapper mapper = new ObjectMapper();
	
	@Parameter(names="--get")
	String get;
	
	@Parameter(names="--get-all")
	boolean getAll;
	
	@Parameter(names="--set",arity=2,description="sets a config value")
	List<String> set;
	
	@Override
	public void execute() throws IOException {
		
		if (getAll) {
			System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(getConfig()));
		}
		else if (get!=null) {
			System.out.println(getConfig().path(get).asText());
		}
		else if (set!=null) {
			getConfig().put(set.get(0), set.get(1));
			getCLI().getConfigManager().saveConfig();
		}
		else {
			throw new ParameterException("");
		}
	}

}
