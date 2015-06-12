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
package io.macgyver.core.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class MacGyverTaskCollectorTest {

	@Test
	public void testIt() {
		MacGyverTaskCollector c = new MacGyverTaskCollector();
		assertThat(c.enhanceCronExpression("* * * * *")).isEqualTo("* * * * *");
		
		assertThat(c.isEnabled()).isTrue();
		
		c.setEnabled(false);
		
		assertThat(c.isEnabled()).isFalse();
    }
}
