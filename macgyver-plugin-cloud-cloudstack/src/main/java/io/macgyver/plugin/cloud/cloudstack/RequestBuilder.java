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

import io.macgyver.core.MacGyverConfigurationException;
import io.macgyver.core.MacGyverException;
import io.macgyver.okrest.OkRestTarget;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import okio.Buffer;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.BaseEncoding;
import com.squareup.okhttp.FormEncodingBuilder;

public class RequestBuilder {

	Map<String, String> paramMap = Maps.newHashMap();

	CloudStackClientImpl client;

	public RequestBuilder command(String n) {
		return param("command", n);
	}

	public RequestBuilder param(String key, String val) {
		paramMap.put(key, val);
		return this;
	}

	protected String generateCommandStringForHmac() {

		try {
			Map<String, String> copy = Maps.newHashMap();
			for (Map.Entry<String, String> entry : paramMap.entrySet()) {
				copy.put(entry.getKey().toLowerCase(), entry.getValue()
						.toLowerCase());
			}
			List<String> sortedKeys = Lists.newArrayList(copy.keySet());
			Collections.sort(sortedKeys);

			List<String> kvPairs = Lists.newArrayList();
			for (String x : sortedKeys) {
				String kv = URLEncoder.encode(x, "UTF-8") + "="
						+ URLEncoder.encode(copy.get(x), "UTF-8");
				kvPairs.add(kv);
			}

			return Joiner.on("&").join(kvPairs);

		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	protected String computeSignature() {

		Preconditions.checkNotNull(client, "client cannot be null");
		Preconditions.checkState(!Strings.isNullOrEmpty(client.secretKey),
				"secretKey not set");
		String cs = generateCommandStringForHmac();

		String m = hmacSha1(cs, client.secretKey);

		return m;

	}

	protected String hmacSha1(String value, String signingKeyString) {
		try {
			// Get an hmac_sha1 key from the raw key bytes
			byte[] keyBytes = signingKeyString.getBytes();
			SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");

			// Get an hmac_sha1 Mac instance and initialize with the signing key
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(signingKey);

			// Compute the hmac on input data bytes
			byte[] rawHmac = mac.doFinal(value.getBytes());

			// Convert raw bytes to Hex
			return BaseEncoding.base64Url().encode(rawHmac);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public RequestBuilder listVirtualMachines() {
		return command("listVirtualMachines");
	}
	public JsonNode execute() {

		try {
			OkRestTarget t = client.target;
			param("response", "json");

			String expires = DateTimeFormat.forPattern(
					"yyyy-MM-dd'T'HH:mm:ssZZ").print(
					System.currentTimeMillis() + 1000 * 60 * 5);
			param("expires", expires);
			FormEncodingBuilder b = new FormEncodingBuilder();
			for (Map.Entry<String, String> entry : paramMap.entrySet()) {
				b = b.add(entry.getKey(), entry.getValue());

			}

			if (client.usernamePasswordAuth) {
				String sessionKey = client.getSessionData()
						.path("loginresponse").path("sessionkey").asText();

				b = b.add("sessionkey", sessionKey).add("expires", expires)
						.add("signatureversion", "3");

				t = t.header("Cookie", "JSESSIONID="
						+ client.getSessionData().path("JSESSIONID").asText());

			} else {
				b = b.add("apiKey", client.apiKey).add("signature",
						computeSignature());

			}

			return t.post(b.build()).execute(JsonNode.class);
		} catch (ExecutionException e) {
			throw new MacGyverException(e);
		}
	}
}
