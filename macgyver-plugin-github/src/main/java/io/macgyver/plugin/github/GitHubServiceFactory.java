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
package io.macgyver.plugin.github;


import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.lendingclub.mercator.core.Projector;
import org.lendingclub.mercator.github.GitHubScanner;
import org.lendingclub.mercator.github.GitHubScannerBuilder;

import com.google.common.base.Strings;

import io.macgyver.core.Kernel;
import io.macgyver.core.service.BasicServiceFactory;
import io.macgyver.core.service.ServiceDefinition;
import io.macgyver.core.service.ServiceRegistry;

public class GitHubServiceFactory extends BasicServiceFactory<GitHub> {

	public GitHubServiceFactory() {
		super("github");
	}

	@Override
	protected GitHub doCreateInstance(ServiceDefinition def) {
		try {
			Properties props = def.getProperties();
			String url = props.getProperty("url");
			String oauthToken = props.getProperty("oauthToken");
			String username = props.getProperty("username");
			String password = props.getProperty("password");

			boolean useToken = Strings.isNullOrEmpty(oauthToken) == false;
			boolean useUsernamePassword = (Strings.isNullOrEmpty(username) == false)
					&& (Strings.isNullOrEmpty(password) == false);

			String message = "connecting to {} using {}";

			GitHubBuilder builder = new GitHubBuilder();

		
			GitHub gh = null;

			if (url != null) {
				builder = builder.withEndpoint(url);
			} else {
				builder = builder.withEndpoint("https://api.github.com");
			}

			if (useToken) {
				logger.info(message, url, "oauth");
				builder = builder.withOAuthToken(oauthToken);

			} else if (useUsernamePassword) {
				logger.info(message, url, "username/password");
				builder = builder.withPassword(username, password);
			}

			gh = builder.build();

			return gh;
		} catch (IOException e) {
			throw new io.macgyver.core.ConfigurationException(
					"problem creating GitHub client", e);
		}
	}

	@Override
	protected void doCreateCollaboratorInstances(ServiceRegistry registry,
			ServiceDefinition primaryDefinition, Object primaryBean) {

		io.macgyver.okrest3.OkRestClient.Builder builder = new io.macgyver.okrest3.OkRestClient.Builder();
		
		GitHub h;

		final String oauthToken = primaryDefinition.getProperties()
				.getProperty("oauthToken");
		final String username = primaryDefinition.getProperties().getProperty(
				"username");
		final String password = primaryDefinition.getProperties().getProperty(
				"password");
		String url = primaryDefinition.getProperties().getProperty("url");
		if (!Strings.isNullOrEmpty(oauthToken)) {
			logger.info("using oauth");
			builder = builder.withBasicAuth(oauthToken, "x-oauth-token");
			

		} else if (!Strings.isNullOrEmpty(username)) {
			logger.info("using username/password auth for OkRest client: "
					+ username + "/" + password);
			builder = builder.withBasicAuth(username, password);
		
		} else {
			logger.info("using anonymous auth");
		}

		if (Strings.isNullOrEmpty(url)) {
			url = "https://api.github.com";
		}
		io.macgyver.okrest3.OkRestTarget rest = builder.build().url(url);

		registry.registerCollaborator(primaryDefinition.getName() + "Api", rest);

		GitHubScannerBuilder scannerBuilder = Kernel.getApplicationContext().getBean(Projector.class).createBuilder(GitHubScannerBuilder.class);
		if (!Strings.isNullOrEmpty(url)) {
			scannerBuilder = scannerBuilder.withUrl(url);
		}
		if (!Strings.isNullOrEmpty(username)) {
			scannerBuilder = scannerBuilder.withUsername(username);
		}
		if (!Strings.isNullOrEmpty(password)) {
			scannerBuilder = scannerBuilder.withPassword(password);
		}
		if (!Strings.isNullOrEmpty(oauthToken)) {
			scannerBuilder = scannerBuilder.withToken(oauthToken);
		}
		GitHubScanner scanner = scannerBuilder.build();
		
		registry.registerCollaborator(primaryDefinition.getName()+"Scanner", scanner);
	}

	@Override
	public void doCreateCollaboratorDefinitions(Set<ServiceDefinition> defSet,
			ServiceDefinition def) {
		ServiceDefinition templateDef = new ServiceDefinition(def.getName()
				+ "Api", def.getName(), def.getProperties(), this);
		defSet.add(templateDef);

		
		templateDef = new ServiceDefinition(def.getName()
				+ "Scanner", def.getName(), def.getProperties(), this);
		defSet.add(templateDef);
	}

}
