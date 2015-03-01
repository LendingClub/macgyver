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
package io.macgyver.plugin.github;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;

public class SignatureBasedWebHookAuthenticator extends WebHookAuthenticator {

	public static final String HEADER_NAME = "X-Hub-Signature";
	Logger logger = LoggerFactory
			.getLogger(SignatureBasedWebHookAuthenticator.class);

	String secret;

	public SignatureBasedWebHookAuthenticator(String secret) {
		Preconditions.checkNotNull(secret);
		this.secret = secret;
	}

	protected String getSecret() {
		return secret;
	}

	public Optional<Boolean> authenticate(WebHookEvent event,
			HttpServletRequest request) {

		try {
			boolean b = verifySignature(event.getRawData(), request,
					getSecret());

			if (b == false) {
				return Optional.absent();
			} else {
				return Optional.of(true);
			}
		} catch (GeneralSecurityException e) {
			logger.warn("", e);
			return Optional.absent();
		}
	}

	protected boolean verifySignature(byte[] bytes, HttpServletRequest request,
			String secret) throws InvalidKeyException, NoSuchAlgorithmException {
		String signature = request.getHeader(HEADER_NAME);

		String result = calculateSha1(bytes, secret).toLowerCase();

		boolean b = result.equalsIgnoreCase(signature);

		if (logger.isDebugEnabled()) {
			logger.debug("computed HMAC: {}", result);
			logger.info("{}: {}", HEADER_NAME, signature);
			logger.info("HMAC match: {}", b);

		}

		return b;

	}

	protected String calculateSha1(byte[] bytes, String secret)
			throws NoSuchAlgorithmException, InvalidKeyException {
		String result = "sha1=";
		String key = secret;

		SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), "HmacSHA1");

		Mac mac = Mac.getInstance("HmacSHA1");
		mac.init(signingKey);

		byte[] rawHmac = mac.doFinal(bytes);

		result += BaseEncoding.base16().encode(rawHmac);

		return result;
	}
}
