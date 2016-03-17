package io.macgyver.discovery.agent;

import java.io.IOException;
import java.util.Properties;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

public class RestSenderTest {

	@Rule
	public MockWebServer mockServer = new MockWebServer();
	
	@Test
	public void testIt() throws IOException, InterruptedException {
		RestSender s = new RestSender();
		Properties cfg = new Properties();
		
		cfg.setProperty("url", mockServer.url("/api/cmdb/checkIn").toString());
		System.out.println(cfg);
		
		s.configure(cfg);
		
		
		mockServer.enqueue(new MockResponse().setBody("{}"));
		
		Properties data = new Properties();
		data.put("foo", "bar");
		data.put("hello", "world");
		s.send(data);
		
		RecordedRequest rr = mockServer.takeRequest();
		
		Assertions.assertThat(rr.getMethod()).isEqualTo("POST");
		Assertions.assertThat(rr.getPath()).isEqualTo("/api/cmdb/checkIn");
		Assertions.assertThat(rr.getHeader("content-type")).isEqualTo("application/x-www-form-urlencoded");
		Assertions.assertThat(rr.getBody().readUtf8()).contains("foo=bar").contains("&").contains("hello=world");
		
	}
}
