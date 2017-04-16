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
package io.macgyver.plugin.github;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class SignautreBasedWebHookAuthenticatorTest {

	
	@Test
	public void testSha1() throws GeneralSecurityException, UnsupportedEncodingException {
		SignatureBasedWebHookAuthenticator s = new SignatureBasedWebHookAuthenticator("secret");
		
		Assertions.assertThat(s.getSecret()).isEqualTo("secret");
		
		byte [] b = "{\"payload\":\"test\"}".getBytes("UTF8");
		
		Assertions.assertThat(s.calculateSha1(b, "secret")).isEqualTo("sha1=914325B95BE16BBA47A9650FCB916625858ECA7F");
		
	}
}
