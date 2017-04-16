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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.macgyver.core.cli;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.macgyver.cli.CLIException;
import io.macgyver.cli.CLIRemoteException;
import io.macgyver.cli.TestCLI;
import io.macgyver.cli.TestCLI.MemoryConfigManager;
import io.macgyver.core.cli.LoginCommand;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class LoginCommandTest {

	ObjectMapper mapper = new ObjectMapper();

	@Rule
	public MockWebServer mockServer = new MockWebServer();

	@Test
	public void testIt() throws IOException, InterruptedException {

		TestCLI cli = new TestCLI().withConfig("url", mockServer.url("").toString()).withConfig("token", "dummytoken");

		ObjectNode response = mapper.createObjectNode().put("token", "newtoken");
		mockServer.enqueue(new MockResponse().setBody(response.toString()));

		Assertions.assertThat(cli.getConfigManager()).isInstanceOf(MemoryConfigManager.class);

		cli.run("login", "--refresh");

		RecordedRequest rr = mockServer.takeRequest();

		Assertions.assertThat(rr.getHeader("Authorization")).isEqualTo("Token dummytoken");

		Assertions.assertThat(cli.getConfigManager().getData().path("token").asText()).isEqualTo("newtoken");

	}

	@Test
	public void test403() throws IOException, InterruptedException {

		TestCLI cli = new TestCLI().withConfig("url", mockServer.url("").toString()).withConfig("token",
				"dummytoken");

		ObjectNode response = mapper.createObjectNode().put("token", "newtoken");
		mockServer.enqueue(new MockResponse().setResponseCode(403));

		Assertions.assertThat(cli.getConfigManager()).isInstanceOf(MemoryConfigManager.class);

		boolean b = cli.run("token", "--refresh", "--debug");

		Assertions.assertThat(b).isFalse();

	}

	@Test
	public void testResponseWithoutToken() throws IOException, InterruptedException {

			TestCLI cli = new TestCLI().withConfig("url", mockServer.url("").toString()).withConfig("missingdata",
					"dummytoken");

			ObjectNode response = mapper.createObjectNode().put("missingtoken", "newtoken");
			mockServer.enqueue(new MockResponse().setBody(response.toString()));

			Assertions.assertThat(cli.getConfigManager()).isInstanceOf(MemoryConfigManager.class);

			boolean b = cli.run("token", "--refresh", "--debug");

			Assertions.assertThat(b).isFalse();

	

	}

	@Test
	public void testHelp() throws IOException, InterruptedException {

		try {
			TestCLI cli = new TestCLI().withConfig("url", mockServer.url("").toString()).withConfig("token",
					"dummytoken");

			ObjectNode response = mapper.createObjectNode().put("token", "newtoken");
			mockServer.enqueue(new MockResponse().setResponseCode(403));

			Assertions.assertThat(cli.getConfigManager()).isInstanceOf(MemoryConfigManager.class);

			boolean b = cli.run("login", "--invalid");

			Assertions.assertThat(b).isFalse();

		} catch (Exception e) {
			Assertions.assertThat(e).isInstanceOf(CLIRemoteException.class);
		}

	}
}
