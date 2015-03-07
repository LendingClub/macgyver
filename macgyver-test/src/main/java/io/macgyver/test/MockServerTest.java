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
