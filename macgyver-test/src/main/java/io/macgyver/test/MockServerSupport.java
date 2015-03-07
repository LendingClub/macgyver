package io.macgyver.test;

import org.mockserver.client.server.MockServerClient;
import org.mockserver.mock.Expectation;

public interface MockServerSupport {

	


	public MockServerClient getMockServerClient();
	public String getMockServerUrl();
	public Expectation getFirstExpectation();
	
}
