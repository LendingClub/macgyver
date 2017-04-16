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

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class ApiTokenTest {

	@Test
	public void testIt() {
		ApiToken t = ApiToken.createRandom();
		
		String armored = t.getArmoredString();
		
		
		ApiToken t2 = ApiToken.parse(armored);
		
		Assertions.assertThat(t.getSecretKey()).isEqualTo(t2.getSecretKey());
		Assertions.assertThat(t.getAccessKey()).isEqualTo(t2.getAccessKey());
		Assertions.assertThat(t.getArmoredString()).isEqualTo(t2.getArmoredString());
		
		System.out.println(t.getArmoredString());
	}

}
