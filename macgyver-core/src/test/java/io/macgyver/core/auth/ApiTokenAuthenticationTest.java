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

import org.junit.Test;

import com.lambdaworks.crypto.SCryptUtil;

public class ApiTokenAuthenticationTest {

	@Test
	public void testIt() {
		System.out.println(SCryptUtil.scrypt("test", 4096, 8, 1));
		System.out.println(SCryptUtil.scrypt("test", 4096, 8, 1));
	}

}
