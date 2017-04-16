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
package io.macgyver.plugin.metrics;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class RexexMetricFilterTest {

	@Test
	public void testIt() {
		RegexMetricFilter f = new RegexMetricFilter().excludes(".*counter\\..*");
		
		
		Assertions.assertThat(f.matches("foo", null)).isTrue();
		Assertions.assertThat(f.matches("abc.counter.test", null)).isFalse();
	}
	
	
	@Test
	public void testDefaults() {
	
		Assertions.assertThat(new RegexMetricFilter().matches("foo", null)).isTrue();
		Assertions.assertThat(new RegexMetricFilter().excludes("  ").matches("foo", null)).isTrue();
		Assertions.assertThat(new RegexMetricFilter().excludes(null).matches("foo", null)).isTrue();
		
	}

}
