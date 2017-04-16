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

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

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
		CloudStackClientImpl c = new CloudStackClientImpl(mockServer.url(
				"/client/api").toString()).usernamePasswordAuth("scott",
				"tiger");
//		c.target.getOkHttpClient().interceptors().add(new okhttp3.logging.HttpLoggingInterceptor());
		
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
		CloudStackClientImpl c = new CloudStackClientImpl(mockServer.url(
				"/client/api").toString()).apiKeyAuth("myAccessKey", "mySecretKey");
	
		
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
		CloudStackClientImpl c = new CloudStackClientImpl(mockServer.url(
				"/client/api").toString()).apiKeyAuth("key", "secret");

		RequestBuilder b = c.newRequest();

	
		JsonNode n = b.param("a", "b").execute();

		RecordedRequest rr = mockServer.takeRequest();

		Assertions.assertThat(rr.getBody().readUtf8()).contains("apiKey=key","a=b","response=json","signature=");
		// Assertions.assertThat(rr.getPath()).isEqualTo("xx");

	}
	

}
