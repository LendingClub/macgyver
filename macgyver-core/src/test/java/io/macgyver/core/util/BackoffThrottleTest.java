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
package io.macgyver.core.util;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class BackoffThrottleTest {

	
	@Test
	public void testIt() {
		
		long t0 = System.currentTimeMillis();
		BackoffThrottle.sleep(500L);
		long t1 = System.currentTimeMillis();
		
		Assertions.assertThat(t1-t0).isGreaterThanOrEqualTo(500L);
	}
	
	
	@Test
	public void testX() {
		BackoffThrottle b = new BackoffThrottle();
		
		Assertions.assertThat(b.getDelay()).isEqualTo(0);
		b.markFailure();
	
		Assertions.assertThat(b.getDelay()).isEqualTo(100);
		b.markFailure();
		Assertions.assertThat(b.getDelay()).isEqualTo(400);
		b.markSuccess();
		Assertions.assertThat(b.getDelay()).isEqualTo(0);
	}

}
