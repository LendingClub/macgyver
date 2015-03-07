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
package io.macgyver.test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.mock.Expectation;
import org.mockserver.socket.PortFactory;
import org.slf4j.LoggerFactory;

public class MockServerTest implements MockServerSupport {

	// mockServer will be set automatically by the MockServerRule
	static private MockServerMixin mockServerMixin;


	@BeforeClass
	public static void setupMockServer() {
		mockServerMixin = new MockServerMixin();
		
	}

	@AfterClass
	public static void cleanupMockServer() {
		mockServerMixin.stop();
	}

	public MockServerClient getMockServerClient() {
		return mockServerMixin.getMockServerClient();
	}

	public String getMockServerUrl() {
		return mockServerMixin.getMockServerUrl();
	}

	@Override
	public Expectation getFirstExpectation() {
		return mockServerMixin.getFirstExpectation();
	}

}
