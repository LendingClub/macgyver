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
package io.macgyver.plugin.cloud.cloudstack;

import io.macgyver.core.util.StandaloneServiceBuilder;
import io.macgyver.okrest.OkRestLoggingInterceptor;
import io.macgyver.okrest.compat.OkUriBuilder;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import com.squareup.okhttp.mockwebserver.MockWebServer;

public class CloudStackClientImplTest {

	Logger logger = LoggerFactory.getLogger(CloudStackClientImplTest.class);
	@Rule
	public MockWebServer mockServer = new MockWebServer();

	@Test
	public void testUsernamePasswordAuth() throws InterruptedException {
		String loginResponse = "{\n" + 
				"    \"loginresponse\": {\n" + 
				"        \"account\": \"scott\",\n" + 
				"        \"domainid\": \"ffffffff-817d-11e4-a82a-ffffffffffff\",\n" + 
				"        \"firstname\": \"Bruce\",\n" + 
				"        \"lastname\": \"Scott\",\n" + 
				"        \"registered\": \"false\",\n" + 
				"        \"sessionkey\": \"AWyCGTaFFFFFFFFFFFFf4w0LKws=\",\n" + 
				"        \"timeout\": \"1800\",\n" + 
				"        \"type\": \"1\",\n" + 
				"        \"userid\": \"ffffffff-817d-11e4-a82a-000000000000\",\n" + 
				"        \"username\": \"scott\"\n" + 
				"    }\n" + 
				"}";
		mockServer.enqueue(new MockResponse().setBody(loginResponse).setHeader("Set-Cookie", "JSESSIONID=ABCDEF;"));
		mockServer.enqueue(new MockResponse().setBody("{}"));
		CloudStackClientImpl c = new CloudStackClientImpl(mockServer.getUrl(
				"/client/api").toExternalForm()).usernamePasswordAuth("scott",
				"tiger");
		c.target.getOkHttpClient().interceptors().add(new OkRestLoggingInterceptor());
		
		JsonNode n = c.newRequest().command("test").execute();
		
		Assertions.assertThat(n).isNotNull();
	

		RecordedRequest rr = mockServer.takeRequest();
		Assertions.assertThat(rr.getMethod()).isEqualTo("POST");
		Assertions.assertThat(rr.getPath()).isEqualTo("/client/api");
		

		Assertions.assertThat(rr.getBody().readUtf8()).contains("username=scott","password=tiger","response=json","command=login");

		Assertions.assertThat(c.cache.getIfPresent(CloudStackClientImpl.CACHE_KEY)).isNotNull().isInstanceOf(JsonNode.class);
		
	
		rr = mockServer.takeRequest();
		Assertions.assertThat(rr.getMethod()).isEqualTo("POST");
		Assertions.assertThat(rr.getHeader("Cookie")).contains("ABCDEF");
	
		Assertions.assertThat(rr.getBody().readUtf8()).contains("response=json","command=test","sessionkey=AWyCGTaFFFFFFFFFFFFf4w0LKws%3D");
	}

	
	
	@Test
	public void testAccessKeyAuth() throws InterruptedException {
		String loginResponse = "{\n" + 
				"    \"loginresponse\": {\n" + 
				"        \"account\": \"scott\",\n" + 
				"        \"domainid\": \"ffffffff-817d-11e4-a82a-ffffffffffff\",\n" + 
				"        \"firstname\": \"Bruce\",\n" + 
				"        \"lastname\": \"Scott\",\n" + 
				"        \"registered\": \"false\",\n" + 
				"        \"sessionkey\": \"AWyCGTaFFFFFFFFFFFFf4w0LKws=\",\n" + 
				"        \"timeout\": \"1800\",\n" + 
				"        \"type\": \"1\",\n" + 
				"        \"userid\": \"ffffffff-817d-11e4-a82a-000000000000\",\n" + 
				"        \"username\": \"scott\"\n" + 
				"    }\n" + 
				"}";
		mockServer.enqueue(new MockResponse().setBody(loginResponse));
		mockServer.enqueue(new MockResponse().setBody("{}"));
		CloudStackClientImpl c = new CloudStackClientImpl(mockServer.getUrl(
				"/client/api").toExternalForm()).apiKeyAuth("myAccessKey", "mySecretKey");
		c.target.getOkHttpClient().interceptors().add(new OkRestLoggingInterceptor());
		
		JsonNode n = c.newRequest().command("test").execute();
		
		Assertions.assertThat(n).isNotNull();
	

		RecordedRequest rr = mockServer.takeRequest();
		Assertions.assertThat(rr.getMethod()).isEqualTo("POST");
		Assertions.assertThat(rr.getPath()).isEqualTo("/client/api");
		


		Assertions.assertThat(rr.getBody().readUtf8()).contains("response=json","command=test","signature=");

	
	}
	@Test
	public void testIt() throws InterruptedException {

		mockServer.enqueue(new MockResponse().setBody("{}"));
		CloudStackClientImpl c = new CloudStackClientImpl(mockServer.getUrl(
				"/client/api").toExternalForm()).apiKeyAuth("key", "secret");

		RequestBuilder b = c.newRequest();

	
		JsonNode n = b.param("a", "b").execute();

		RecordedRequest rr = mockServer.takeRequest();

		Assertions.assertThat(rr.getBody().readUtf8()).contains("apiKey=key","a=b","response=json","signature=");
		// Assertions.assertThat(rr.getPath()).isEqualTo("xx");

	}
	

}
