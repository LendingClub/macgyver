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
