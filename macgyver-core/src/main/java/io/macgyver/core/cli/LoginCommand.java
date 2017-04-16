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

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;

import io.macgyver.cli.CLI;
import io.macgyver.cli.CLIException;
import io.macgyver.cli.CLIRemoteException;
import io.macgyver.cli.Command;
import io.macgyver.okrest3.OkRestClient;
import io.macgyver.okrest3.OkRestResponse;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;


@Parameters()
public class LoginCommand  extends Command {

	Logger logger = LoggerFactory.getLogger(LoginCommand.class);

	@Parameter(names={"--url"},arity=1,description="url for the MacGyver server")
	String url;
	
	@Parameter(names={"--refresh"}, description="refresh the API token")
	boolean refresh=false;
	
	@Parameter(names={"--username"}, arity=1, description="username to log in")
	String username;
	
	@Override
	public void execute() throws IOException {
		if (refresh) {
			executeRefreshToken();
		}
		else {
			executeLogin();
		}
	}
	

	Optional<char[]> readPassword() {
		char[] p = System.console().readPassword("%s", "Password: ");
		return Optional.ofNullable(p);
	}

	public Optional<String> readUsername() {
		String username = getConfig().path("username").asText();
		if (Strings.isNullOrEmpty(username)) {
			username = System.getProperty("user.name","");
		}
		String usernameFromConsole = Strings.nullToEmpty(System.console().readLine("username [%s]: ", username)).trim();		
		
		if (!Strings.isNullOrEmpty(usernameFromConsole)) {
			username = usernameFromConsole;
		}
		logger.info("username: " + username);	
		return Optional.ofNullable(username);
	}

	public Optional<String> getUsername() {

		Optional<String> optUser = Optional.empty();

	
		if (!Strings.isNullOrEmpty(username)) {
			logger.debug("username from command line is: {}", username);
			return Optional.ofNullable(username);
		}

		
		if (!Strings.isNullOrEmpty(username)) {
			logger.debug("username from config file is: {}", username);
			return Optional.of(username.trim());
		}

		return Optional.empty();
	}
	
	

	public void executeLogin() throws IOException {
		Optional<String> url = Optional.ofNullable(this.url);
		
		if (!url.isPresent()) {
			url = Optional.ofNullable(getConfig().path("url").asText(null));
			
		}
		if (!url.isPresent()) {
			url = readUrl();
		}
		
		Optional<String> username = getUsername();
		if (!username.isPresent()) {
			username = readUsername();
		}
		
		Optional<char []> password = readPassword();
		
		String header = username.get() + ":" + new String(password.get());


		getConfig().put("url", url.get());
		
		header = "Basic " + BaseEncoding.base64().encode(header.getBytes());
		
		
		statusOutput("authenticating with "+getServerUrl());
		OkRestResponse rr = new OkRestClient.Builder().withOkHttpClientConfig(cfg->{
			if (isDebugEnabled()) {
				cfg.addInterceptor(new HttpLoggingInterceptor(new okhttp3.logging.HttpLoggingInterceptor.Logger() {

					@Override
					public void log(String message) {
						logger.debug("{}",message);
						
					}}));
			}
		}). build().uri(getServerUrl()).path("/api/core/token/create").addHeader("Authorization", header).get()
				.execute();

		if (!rr.response().isSuccessful()) {
			int rc = rr.response().code();
			if (rc==401) {
				exitWithError("authentication for '"+username.get()+"' failed");
			}
			else {
				exitWithError("server response code: "+rc);
			}
		}
		JsonNode n = rr.getBody(JsonNode.class);
	

		getConfig().put("username", n.path("username").asText());
		getConfig().put("token",n.path("token").asText());

		getCLI().getConfigManager().saveConfig();

		statusOutput("authentication successful: api token granted");
		statusOutput("token valid until: "+new Date(n.path("expirationTs").asLong(0)));
		statusOutput("run 'macgyver login --refresh' to obtain a new token with an extended TTL");
	}
	

	public Optional<String> readUrl() {
		String url = null;
		
		if (Strings.isNullOrEmpty(url)) {
			url = getConfig().path("url").asText();
		}
		String newVal = System.console().readLine("url      [%s]: ", url);
		if (!Strings.isNullOrEmpty(newVal.trim())) {
			url = newVal;
		}
		getConfig().put("url", url);
		return Optional.ofNullable(url);
	}
	
	public void executeRefreshToken() throws IOException {
		OkRestResponse rr = getOkRestTarget().path("/api/core/token/refresh").post(mapper.createObjectNode()).execute();

		Response response = rr.response();

		if (response.isSuccessful()) {
			
			JsonNode n = rr.getBody(JsonNode.class);
			String token = n.path("token").asText();
			
			if (Strings.isNullOrEmpty(token)) {
			
				throw new CLIException("invalid response from server");
			}
			getConfig().put("token", token);
			getCLI().getConfigManager().saveConfig();
			
			
			
			statusOutput("successfully refreshed API token");
			statusOutput("token valid until: "+new Date(n.path("expirationTs").asLong(0)));
		}
		else {
			throw new CLIRemoteException("could not refresh token", response.code());
		}
	}



	
	
}
