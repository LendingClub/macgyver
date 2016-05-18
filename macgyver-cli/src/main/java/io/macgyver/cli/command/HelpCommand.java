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
import java.util.Collections;
import java.util.List;
import com.beust.jcommander.Parameters;
import com.google.common.collect.Lists;

import io.macgyver.cli.Command;

@Parameters(hidden=true)
public class HelpCommand extends Command {

	@Override
	public void execute() throws IOException {

		List<String> x = Lists.newArrayList(getCLI().getCommandMap().keySet());

		Collections.sort(x);

		System.out.println("usage: macgyver <command> [<args...>]");
		System.out.println("");
		System.out.println("These are common MacGyver commands used in various situations:");
		x.forEach( it -> {
			System.out.println(it);
		});
	}
}
