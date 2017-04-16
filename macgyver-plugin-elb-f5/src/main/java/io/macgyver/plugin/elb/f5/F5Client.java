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
package io.macgyver.plugin.elb.f5;

import java.util.Properties;

import com.google.common.base.Strings;

import io.macgyver.core.service.ServiceFactory;
import io.macgyver.okrest3.OkHttpClientConfigurer;
import io.macgyver.okrest3.OkRestClient;
import io.macgyver.okrest3.OkRestClientConfigurer;
import io.macgyver.okrest3.OkRestTarget;

public class F5Client {

	OkRestTarget target;
	
	public static class Builder {
		
		
		String url;
		String username;
		String password;
		boolean validateCerts=true;
		io.macgyver.okrest3.OkRestClient.Builder f5ClientBuilder;
		
		public Builder() {
			 f5ClientBuilder = new OkRestClient.Builder();
		}
		public Builder withUrl(String url) {
			this.url = url;
			return this;
		}
		
		public Builder withCredentials(String username, String password) {
			this.username = username;
			this.password = password;
			return this;
		}
		
		public Builder withProperties(Properties p) {
			
			String username = p.getProperty("username");
			String password = p.getProperty("password");
			String url = p.getProperty("url");
			
			if (username!=null && password!=null) {
				withCredentials(username,password);
			}
			
			if (url!=null) {
				withUrl(url);
			}
			boolean certVerificationEnabled = Boolean.parseBoolean(p.getProperty(ServiceFactory.CERTIFICATE_VERIFICATION_ENABLED, "true"));
			withCertificateVerificationEnabled(certVerificationEnabled);
			return this;
		}
		public Builder withCertificateVerificationEnabled(boolean b) {
			validateCerts = b;
			return this;
		}
		public Builder withOkRestClientConfig(OkRestClientConfigurer cfg) {
			f5ClientBuilder = f5ClientBuilder.withOkRestClientConfig(cfg);
			return this;
		}
		public Builder withOkHttpClientConfig(OkHttpClientConfigurer cfg) {
			f5ClientBuilder = f5ClientBuilder.withOkHttpClientConfig(cfg);
			return this;
		}
		public F5Client build() {
			
			F5Client client = new F5Client();			
			
			if (!Strings.isNullOrEmpty(username) && !Strings.isNullOrEmpty(password)) {
				f5ClientBuilder = f5ClientBuilder.withBasicAuth(username, password);
			}
			if (!validateCerts) {
				f5ClientBuilder = f5ClientBuilder.disableCertificateVerification();
			}

			client.target = f5ClientBuilder.build().uri(url);
				
			return client;
		}
	}
	
	public OkRestTarget getTarget() {
		return target;
	}
	
}
