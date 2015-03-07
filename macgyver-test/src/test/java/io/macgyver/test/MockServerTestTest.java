package io.macgyver.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.CharStreams;

public class MockServerTestTest extends MockServerTest {

	Logger logger = LoggerFactory.getLogger(MockServerTestTest.class);
	
	
	
	@Test
	public void demonstrateIt() throws Exception {
		
	
		String listenUrl = getMockServerUrl();
		
		logger.info("mock server: {}",listenUrl);
		
	
		
		getMockServerClient().when(HttpRequest.request().withPath("/greet")).respond(HttpResponse.response().withBody("hello"));
		
		
		// Now connect to the mock server and get the greeting
		URL url = new URL(listenUrl+"/greet");
		
		HttpURLConnection c = (HttpURLConnection) url.openConnection();
		
		c.connect();
		
		
		assertThat(c.getResponseCode()).isEqualTo(200);
		assertThat(CharStreams.toString( new InputStreamReader( c.getInputStream(), "UTF-8" ) )).isEqualTo("hello");
		
	
	
	}
}
