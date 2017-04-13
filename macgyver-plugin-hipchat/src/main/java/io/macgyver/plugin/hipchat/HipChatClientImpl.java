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
package io.macgyver.plugin.hipchat;



import io.macgyver.core.service.ProxyConfig;
import io.macgyver.core.service.ProxyConfigManager;
import io.macgyver.okrest3.OkRestResponse;
import io.macgyver.okrest3.OkRestTarget;
import okhttp3.Interceptor;
import okhttp3.Response;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;


public class HipChatClientImpl implements HipChatClient {

	public static final String HOSTED_URL = "https://api.hipchat.com";

	Logger logger = LoggerFactory.getLogger(HipChatClientImpl.class);
	String token;
	io.macgyver.okrest3.OkRestClient okRestClient;
	io.macgyver.okrest3.OkRestTarget base;
	String url;
	ObjectMapper mapper = new ObjectMapper();
	String version = "v2";

	public static class BearerTokenInterceptor implements Interceptor {

		private String token;

		public BearerTokenInterceptor(String token) {
			this.token = token;
		}

		@Override
		public Response intercept(Chain chain) throws IOException {

			return chain.proceed(chain.request().newBuilder()
					.addHeader("Authorization", "Bearer " + token).build());

		}

	}

	public HipChatClientImpl(String token) {
		this(HOSTED_URL, token);
	}

	public HipChatClientImpl(String url, String token) {
		this(url,token,null);
	}
	public HipChatClientImpl(String url, String token, ProxyConfig cfg) {
		Preconditions.checkNotNull(url);
		Preconditions.checkNotNull(token);
		this.url = url;
		this.token = token;
		
		io.macgyver.okrest3.OkRestClient.Builder builder = new io.macgyver.okrest3.OkRestClient.Builder();
		builder = builder.withInterceptor(new BearerTokenInterceptor(token));
		
		if (cfg!=null) {
			builder.withOkHttpClientConfig(cc->{
				cfg.getProxyConfigManager().apply(cc, cfg);
				
			});
		}
		okRestClient = builder.build();
		
	
		base = okRestClient.uri(url);
	}

	public OkRestTarget getBaseTarget() {
		return base.path(version);
	}

	public String getToken() {
		return token;
	}

	public String getUrl() {
		return url;
	}

	@Override
	public JsonNode get(String path, Map<String, String> params) {

			OkRestTarget target = getBaseTarget().path(path);
			if (params != null) {
				for (Map.Entry<String, String> entry : params.entrySet()) {
					target = target
							.queryParam(entry.getKey(), entry.getValue());
				}
			}
			
			OkRestResponse response = target.get().execute();
			return response.getBody(JsonNode.class);

			

	}

	@Override
	public JsonNode get(String path, String... args) {
		Map<String, String> m = Maps.newHashMap();
		for (int i = 0; i < args.length; i += 2) {
			m.put(args[i], args[i + 1]);
		}
		return get(path, m);
	}

	@Override
	public void post(String path, JsonNode body) {

			getBaseTarget()
					.path(path)
					.post(body)
					.execute();


	}

	@Override
	public void put(String path, JsonNode body) {

			getBaseTarget()	
				.path(path)
				.put(body)
				.execute();


	}

	@Override
	public void delete(String path) {

			getBaseTarget().path(path).delete().execute();


	}

	@Override
	public void sendRoomNotification(String roomId, String message) {

			ObjectNode n = mapper.createObjectNode();
			n.put("message", message);
			getBaseTarget().path("/room").path(roomId).path("notification")
					.post(n).execute();

	}

	@Override
	public void sendRoomNotification(String roomId, String message, Format format,
			Color color, boolean notify) {
		sendRoomNotification(roomId, message, format.toString(),color.toString(),notify);
		
	}

	@Override
	public void sendRoomNotification(String roomId, String message,
			String format, String color, boolean notify) {
		
			ObjectNode n = mapper.createObjectNode();
			n.put("message", message);
			n.put("notify", notify);
			n.put("message_format", format);
			n.put("color", color);
			
			getBaseTarget().path("/room").path(roomId).path("notification")
					.post(n).execute();

		
	}

}
