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

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import io.macgyver.okrest3.OkRestClient;
import io.macgyver.okrest3.OkRestException;
import io.macgyver.okrest3.OkRestResponse;
import io.macgyver.okrest3.OkRestTarget;
import io.macgyver.okrest3.OkRestWrapperException;
import okhttp3.FormBody;

public class CloudStackClientImpl implements CloudStackClient {

	Logger logger = LoggerFactory.getLogger(CloudStackClientImpl.class);
	boolean usernamePasswordAuth;

	public static final String CACHE_KEY = "key";

	LoadingCache<String, JsonNode> cache = null;
	String apiKey;
	String secretKey;

	String username;
	String password;

	OkRestTarget target;

	public CloudStackClientImpl(String url) {
		target = new OkRestClient.Builder().build().uri(url);
		cache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES)
				.build(new SessionKeyCacheLoader());
	}

	public class SessionKeyCacheLoader extends CacheLoader<String, JsonNode> {

		@Override
		public JsonNode load(String key) throws Exception {
			return login(username, password);
		}

	}

	public CloudStackClientImpl usernamePasswordAuth(String u, String p) {
		usernamePasswordAuth = true;
		this.username = u;
		this.password = p;
		return this;
	}

	public CloudStackClientImpl apiKeyAuth(String accessKey, String secretKey) {
		usernamePasswordAuth = false;
		this.apiKey = accessKey;
		this.secretKey = secretKey;
		
		return this;
	}

	JsonNode getSessionData() throws ExecutionException {

		return cache.get(CACHE_KEY);

	}

	void refreshToken() {
		if (usernamePasswordAuth) {
			try {
				cache.get(CloudStackClientImpl.CACHE_KEY);
			} catch (ExecutionException e) {
				logger.warn("", e);
			}
			JsonNode n = login(username, password);
		}
	}

	@Override
	public RequestBuilder newRequest() {
		RequestBuilder b = new RequestBuilder();
		b.client = this;

		return b;
	}

	public JsonNode login(String username, String password) {

		try {
			OkRestResponse rr = target
					.post(new FormBody.Builder().add("username", username)
							.add("password", password).add("response", "json")
							.add("domain", "/").add("command", "login").build())
					.execute();

			if (rr.response().isSuccessful()) {
				
				
				ObjectNode sessionData = (ObjectNode) new ObjectMapper().readTree(rr.response().body().bytes());
				
				String x = rr.response().header("Set-Cookie");
				Matcher m = Pattern.compile("JSESSIONID=(.*);.*").matcher(x);
				if (m.matches()) {
					sessionData.put("JSESSIONID", m.group(1));
				}
				
				return sessionData;
			} else {
				throw new OkRestException(rr.response().code());
			}
		} catch (IOException e) {
			throw new OkRestWrapperException(e);
		}

	}



}
