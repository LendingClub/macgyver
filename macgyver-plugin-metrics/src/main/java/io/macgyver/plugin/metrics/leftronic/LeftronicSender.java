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
package io.macgyver.plugin.metrics.leftronic;

import io.macgyver.core.MacGyverException;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

public class LeftronicSender {

	public static Logger logger = LoggerFactory
			.getLogger(LeftronicSender.class);
	public static final String DEFAULT_URL = "https://www.leftronic.com/customSend/";
	String url = DEFAULT_URL;
	String apiKey;

	AsyncHttpClient client;

	ObjectMapper mapper = new ObjectMapper();

	public LeftronicSender(AsyncHttpClient client) {
		Preconditions.checkNotNull(client);
		this.client = client;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}


	public void send(ObjectNode n) {
		try {
			String stringPayload = n.toString();
			if (logger.isTraceEnabled()) {
				ObjectNode masked = (ObjectNode) mapper.readTree(stringPayload);
				masked.put("accessKey", "******");
				logger.trace("sending data leftronic: {}", masked);
			}

			AsyncCompletionHandler<String> h = new AsyncCompletionHandler<String>() {

				@Override
				public void onThrowable(Throwable t) {
					logger.warn("problem sending to leftronic: {}",
							t.toString());
					super.onThrowable(t);

				}

				@Override
				public String onCompleted(Response response) throws Exception {
					int sc = response.getStatusCode();
					if (sc >= 300) {
						logger.warn("leftronic response code: {} body: {}", sc,
								response.getResponseBody());
					}
					return null;
				}
			};

			client.preparePost(url).setBody(stringPayload).execute(h);

		} catch (IOException | RuntimeException e) {
			logger.warn("could not send data to leftronic: {}", e.toString());
		}
	}


	public void send(String streamName, Number val) {

		try {
			if (val == null) {
				return;
			}
			if (Strings.isNullOrEmpty(apiKey)) {
				logger.warn("apiKey not set");
				return;
			}
			ObjectNode n = mapper.createObjectNode();

			n.put("accessKey", apiKey);
			n.put("streamName", streamName);

			if (val instanceof Integer) {
				n.put("point", ((Integer) val).intValue());
			} else if (val instanceof Long) {
				n.put("point", ((Long) val).longValue());
			} else {
				n.put("point", val.doubleValue());
			}

			send(n);

			
		} catch (RuntimeException e) {
			logger.warn("could not send data to leftronic: {}", e.toString());
		} finally {

		}

	}

}
