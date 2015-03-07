package io.macgyver.test;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.socket.PortFactory;
import org.slf4j.LoggerFactory;

public class MockServerTest {

	// mockServer will be set automatically by the MockServerRule
	static private MockServerClient mockServer;
	static private int mockServerPort;

	@BeforeClass
	public static void setupMockServer() {
		mockServerPort = PortFactory.findFreePort();
		mockServer = ClientAndServer.startClientAndServer(mockServerPort);

		LoggerFactory.getLogger(MockServerTest.class).info(
				"mock server running on port: {}", mockServerPort);
	}

	@After
	public void cleanupMockServer() {
		mockServer.stop();
	}

	protected MockServerClient getMockServerClient() {
		return mockServer;
	}

	protected String getMockServerUrl() {
		String listenUrl = "http://localhost:" + mockServerPort;
		return listenUrl;
	}

}
