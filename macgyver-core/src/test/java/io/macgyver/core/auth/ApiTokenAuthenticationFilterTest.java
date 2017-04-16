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
package io.macgyver.core.auth;

import java.security.SecureRandom;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.google.common.io.BaseEncoding;

public class ApiTokenAuthenticationFilterTest {


	@Test
	public void testIt() {
		MockHttpServletRequest request  = new MockHttpServletRequest();
		ApiToken apiToken = ApiToken.createRandom();
		
		request.addHeader("Authorization", "Token "+apiToken.getArmoredString());
		
		ApiTokenAuthenticationFilter filter = new ApiTokenAuthenticationFilter();
		Assertions.assertThat(filter.extractToken(request).get().getCredentials()).isEqualTo(apiToken.getArmoredString());
		Assertions.assertThat(filter.extractToken(request).get().isAuthenticated()).isFalse();
		
	}
	
	@Test
	public void testTokenHeader() {
		ApiTokenAuthenticationFilter filter = new ApiTokenAuthenticationFilter();
		
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		Assertions.assertThat(filter.extractTokenString(request).isPresent()).isFalse();
		
		request = new MockHttpServletRequest();
		request.addHeader("X-Api-key", "foobar");
		Assertions.assertThat(filter.extractTokenString(request).get()).isEqualTo("foobar");
		
		
		request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Token from-auth");
		Assertions.assertThat(filter.extractTokenString(request).get()).isEqualTo("from-auth");
		
		request = new MockHttpServletRequest();
		request.addHeader("X-Api-key", "foobar");
		request.addHeader("Authorization", "Token from-auth");
		Assertions.assertThat(filter.extractTokenString(request).get()).isEqualTo("foobar");
		
		
		
		
	}
	@Test
	public void testBasicAuth() {
		MockHttpServletRequest request  = new MockHttpServletRequest();
		request.addHeader("Authorization", "Basic foobar");
		
		ApiTokenAuthenticationFilter filter = new ApiTokenAuthenticationFilter();
		Assertions.assertThat(filter.extractToken(request).isPresent()).isFalse();
		
	}
	
	@Test
	public void testAbsentHeader() {
		MockHttpServletRequest request  = new MockHttpServletRequest();
		
		ApiTokenAuthenticationFilter filter = new ApiTokenAuthenticationFilter();
		Assertions.assertThat(filter.extractToken(request).isPresent()).isFalse();
	
		
	}
}
