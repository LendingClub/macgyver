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
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;

import io.macgyver.cli.Command;
import io.macgyver.okrest3.OkRestClient;
import io.macgyver.okrest3.OkRestResponse;


@Parameters()
public class LoginCommand  extends Command {

	Logger logger = LoggerFactory.getLogger(LoginCommand.class);
	
	
	@Parameter(names={"--url"}, description="URL of macgyver server")
	String urlFromCommandLine;


	public Optional<String> getUsername() {

		Optional<String> optUser = Optional.empty();

		String username = null;//java.util.Objects.toString(optionSetx.valueOf("user"), "").trim();

		if (!Strings.isNullOrEmpty(username)) {
			logger.debug("username from command line is: {}", username);
			return Optional.ofNullable(username);
		}

		
		if (!Strings.isNullOrEmpty(username)) {
			logger.debug("username from config file is: {}", username);
			return Optional.of(username.trim());
		}

		return Optional.of(System.getProperty("user.name"));
	}

	char[] readPassword() {
		char[] p = System.console().readPassword("%s", "Password: ");
		return p;
	}

	


	public String readUrl() {
		String url = this.urlFromCommandLine;
		
		if (Strings.isNullOrEmpty(url)) {
			url = getConfig().path("url").asText();
		}
		String newVal = System.console().readLine("url      [%s]: ", url);
		if (!Strings.isNullOrEmpty(newVal.trim())) {
			url = newVal;
		}
		getConfig().put("url", url);
		return url;
	}
	public String readUsername() {
		String username = getConfig().path("username").asText();
		if (Strings.isNullOrEmpty(username)) {
			username = System.getProperty("user.name","");
		}
		String usernameFromConsole = Strings.nullToEmpty(System.console().readLine("username [%s]: ", username)).trim();		
		
		if (!Strings.isNullOrEmpty(usernameFromConsole)) {
			username = usernameFromConsole;
		}
		logger.info("username: " + username);
	
		return username;
	}

	@Override
	public void execute() throws IOException {

		
		String localUrl = readUrl();
		
	
		String username = readUsername();
		
		char[] password = readPassword();

		String header = username + ":" + new String(password);


		getConfig().put("url", localUrl);
		
		header = "Basic " + BaseEncoding.base64().encode(header.getBytes());
		
	
		
		OkRestResponse rr = new OkRestClient.Builder().build().uri(getServerUrl()).path("/api/core/token/create").addHeader("Authorization", header).get()
				.execute();

		if (!rr.response().isSuccessful()) {
			int rc = rr.response().code();
			if (rc==401) {
				exitWithError("authentication for '"+username+"' failed");
			}
			else {
				exitWithError("server response code: "+rc);
			}
		}
		JsonNode n = rr.getBody(JsonNode.class);
	

		getConfig().put("username", n.path("username").asText());
		getConfig().put("token",n.path("token").asText());

		getCLI().getConfigManager().saveConfig();

		
	}

	public String toString() {
		return MoreObjects.toStringHelper(this).add("name", getCommandName()).toString();
	}

}
