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
package io.macgyver.core.okhttp3;

import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.macgyver.okrest3.BasicAuthInterceptor;
import io.macgyver.okrest3.OkHttpClientConfigurer;
import io.macgyver.okrest3.TLSUtil;
import io.macgyver.okrest3.TLSUtil.TrustAllHostnameVerifier;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;

public class SoftPropertyConfig {

	public static Logger logger = LoggerFactory.getLogger(SoftPropertyConfig.class);

	public static OkHttpClientConfigurer nullOkHttpClientConfigurer() {
		return new OkHttpClientConfigurer() {

			@Override
			public void accept(Builder t) {
			
				// do nothing
				
			}
			
		};
	}
	public static Optional<Integer> safeIntValue(Properties p, String key) {
		if (p == null || key == null) {
			return Optional.empty();
		}
		String val = p.getProperty(key);
		try {

			if (Strings.isNullOrEmpty(val)) {
				return Optional.empty();
			}
			Integer x = Integer.parseInt(val.trim());

			return Optional.of(x);
		} catch (RuntimeException e) {
			logger.warn("could not parse {} as int", val);
		}

		return Optional.empty();

	}

	public static OkHttpClientConfigurer basicAuthConfig(Properties props) {

		if (props==null) {
			return nullOkHttpClientConfigurer();
		}
		

		
		String username = Strings.nullToEmpty(props.getProperty("username")).trim();
		String password = Strings.nullToEmpty(props.getProperty("password")).trim();

		if (Strings.isNullOrEmpty(username) && Strings.isNullOrEmpty(password)) {
			logger.warn("both username and password were empty");
			return nullOkHttpClientConfigurer();
		}
		
		// there are legitimate use-cases for either the username or password to be empty
		
		OkHttpClientConfigurer b = new OkHttpClientConfigurer() {

			@Override
			public void accept(Builder t) {
				if (username != null && password != null) {
					t.addInterceptor(new BasicAuthInterceptor(username, password));
				}
			}
		};
		return b;
	}

	public static OkHttpClientConfigurer certificateVerificationConfig(Properties props) {

		AtomicBoolean enabled = new AtomicBoolean(true);

		String val = props.getProperty("certificateVerificationEnabled");
		if (val != null && val.trim().toLowerCase().equals("false")) {
			enabled.set(false);
		}

		OkHttpClientConfigurer b = new OkHttpClientConfigurer() {

			@Override
			public void accept(Builder t) {

				if (!enabled.get()) {
					logger.warn("disabling certificate verification");
					t.sslSocketFactory(TLSUtil.createTrustAllSSLContext().getSocketFactory());
					t.hostnameVerifier(new TLSUtil.TrustAllHostnameVerifier());
				}
			}
		};

		return b;

	}

	public static OkHttpClientConfigurer timeoutConfig(Properties props) {
		OkHttpClientConfigurer b = new OkHttpClientConfigurer() {

			@Override
			public void accept(Builder t) {
				safeIntValue(props, "readTimeout").ifPresent(val -> {
					t.readTimeout(val, TimeUnit.SECONDS);
				});

				safeIntValue(props, "writeTimeout").ifPresent(val -> {
					t.writeTimeout(val, TimeUnit.SECONDS);
				});

				safeIntValue(props, "connectTimeout").ifPresent(val -> {
					t.connectTimeout(val, TimeUnit.SECONDS);
				});
			}
		};

		return b;
	}
}
