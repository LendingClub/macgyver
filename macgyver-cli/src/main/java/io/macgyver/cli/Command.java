/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package io.macgyver.cli;

 import java.io.IOException;
 import java.util.List;
 import java.util.Optional;
 import java.util.Properties;

 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;

 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.google.common.base.Splitter;
 import com.google.common.base.Strings;

 import io.macgyver.okrest3.OkRestClient;
 import io.macgyver.okrest3.OkRestTarget;



public abstract class Command {

	Logger logger = LoggerFactory.getLogger(getClass());

	List<String> args;

	Properties props = new Properties();

	CLI cli;

  @Parameter(names ="--help", help = true, hidden = true)
  private boolean help = false;

  public boolean getHelp() {
    return help;
  }

	public String getCommandName() {
		List<String> list = Splitter.on(".").splitToList(getClass().getName());
		String name = list.get(list.size() - 1);


		if (name.endsWith("Command")) {
			name = name.substring(0, name.length() - "Command".length());
		}

		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);

			if (i > 0 && Character.isUpperCase(c)) {
				sb.append("-");
			}
			sb.append(Character.toLowerCase(c));
		}

		return sb.toString();

	}



	public final Command init(String[] args) {
		/*
		 * this.args = Collections.unmodifiableList(Lists.newArrayList(args));
		 *
		 * OptionParser p = optionParser;
		 * //p.allowsUnrecognizedOptions();
		 * p.accepts("endpoint-url").withRequiredArg();
		 * p.accepts("user").withRequiredArg();
		 * p.accepts("config").withRequiredArg();
		 * p.accepts("token").withRequiredArg();
		 * p.accepts("debug");
		 * configure(optionParser);
		 *
		 * optionSetx = optionParser.parse(args);
		 * parseConfig();
		 */
		return doInit();

	}

	public List<String> args() {
		return args;
	}

	public Command doInit() {
		return this;
	}

	protected Optional<String> getToken() {
		return Optional.ofNullable(Strings.emptyToNull(getConfig().path("token").asText(null)));
	}

	public abstract void execute() throws IOException;

	OkRestTarget target;

	public OkRestTarget getOkRestTarget() {
		if (target == null) {

			target = new OkRestClient.Builder().build().uri(getServerUrl()).addHeader("Authorization",
					"Token " + getToken().orElse(""));
		}
		return target;
	}

	public ObjectNode getConfig() {
		return getCLI().getConfigManager().getData();
	}


	public String getServerUrl() {


		String url = getConfig().path("url").asText().trim();
		if (Strings.isNullOrEmpty(url)) {
			throw new CLIException("url must be set");
		}
		else if ((!url.startsWith("http://")) && (!url.startsWith("https://"))) {
			throw new CLIException("url must be http(s)");
		}
		return url;
	}



	public CLI getCLI() {
		return cli;
	}

	public void exitWithError(String message) {
		System.err.println("error: "+message);
		System.exit(1);
	}
}
