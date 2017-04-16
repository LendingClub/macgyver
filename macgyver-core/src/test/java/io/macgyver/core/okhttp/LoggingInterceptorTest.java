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
package io.macgyver.core.okhttp;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bouncycastle.crypto.RuntimeCryptoException;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import io.macgyver.core.okhttp.LoggingInterceptor.Level;

public class LoggingInterceptorTest {

	@Rule
	public MockWebServer x = new MockWebServer();
	
	@Test
	public void testIt() throws IOException {
		
		x.enqueue(new MockResponse().setBody("{\"hello\":\"world\"}").addHeader("Content-type","application/json"));
		OkHttpClient c = new OkHttpClient();
		c.interceptors().add(LoggingInterceptor.create(LoggerFactory.getLogger(LoggingInterceptorTest.class),Level.NONE));
		
		c.newCall(new Request.Builder().url(x.url("/foo")).addHeader("Authorization", "foo").build()).execute();
	}
	

	public static class BlowingLoggingInterceptor extends LoggingInterceptor {

		protected BlowingLoggingInterceptor(Logger logger, Level level) {
			super(logger, level);
			
		}

		@Override
		protected boolean isResponseBodySizeWithinLimit(Response response) {
			throw new RuntimeException("simulated error");
		}
		
	}
	

	
	@Test
	public void testResponseException() throws IOException {
		
		x.enqueue(new MockResponse().setBody("{\"hello\":\"world\"}").addHeader("Content-type","application/json"));
		OkHttpClient c = new OkHttpClient();
		
		c.interceptors().add(new BlowingLoggingInterceptor(LoggerFactory.getLogger(BlowingLoggingInterceptor.class),Level.BODY));
		
		c.newCall(new Request.Builder().url(x.url("/foo")).addHeader("Authorization", "foo").build()).execute();
	}

}
