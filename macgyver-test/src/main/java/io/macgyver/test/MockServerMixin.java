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

import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.mock.Expectation;
import org.mockserver.socket.PortFactory;
import org.slf4j.LoggerFactory;

/**
 * Common functionality collected into a "mixin" so that it can be applied to different test base classes.
 * The JUnit Rule pattern works, but this ends up being far more convenient.
 * 
 * @author rschoening
 *
 */
public class MockServerMixin implements MockServerSupport {

	static  MockServerClient mockServer;
	static  int mockServerPort;
	
	
	public MockServerMixin() {
		this(PortFactory.findFreePort());
	}
	public MockServerMixin(int port) {
		this.mockServerPort = port;
		mockServer = ClientAndServer.startClientAndServer(mockServerPort);
		LoggerFactory.getLogger(MockServerMixin.class).info(
				"mock server running on port: {}", mockServerPort);
	}
	public MockServerClient getMockServerClient() {
		return mockServer;
	}

	public String getMockServerUrl() {
		String listenUrl = "http://localhost:" + mockServerPort;
		return listenUrl;
	}
	public Expectation getFirstExpectation() {
		return mockServer.retrieveAsExpectations(null)[0];
	}
	
	public void stop() {
		mockServer.stop();
	}
}
