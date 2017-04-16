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
package io.macgyver.core.scheduler;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.macgyver.core.resource.StringResource;

public class ScheduledTaskManagerTest {

	ObjectMapper mapper = new ObjectMapper();

	ScheduledTaskManager stm = new ScheduledTaskManager();

	@Test
	public void testDefault() {

		Assertions.assertThat(stm.isSchedulerEnabled()).isTrue();

	}

	@Test
	public void testLocalDefault() {
		Assertions.assertThat(stm.isEnabled(mapper.createObjectNode())).isTrue();
	}

	@Test
	public void testEnabled() {
		Assertions.assertThat(stm.isEnabled(mapper.createObjectNode().put("enabled", "true"))).isTrue();
		Assertions.assertThat(stm.isEnabled(mapper.createObjectNode().put("enabled", "xx"))).isTrue();

		Assertions.assertThat(stm.isEnabled(mapper.createObjectNode().put("enabled", "false"))).isFalse();
	}

	@Test
	public void testManualScheduledScript() {

		Assertions.assertThat(stm.isScriptManuallyScheduled(null)).isFalse();
		Assertions.assertThat(stm.isScriptManuallyScheduled(mapper.createObjectNode())).isFalse();
		Assertions.assertThat(stm.isScriptManuallyScheduled(mapper.createObjectNode().put("scheduledBy", "manual")))
				.isTrue();
	}

	@Test
	public void testExtractCron() {
		CrontabExpressionExtractor e = new CrontabExpressionExtractor();
		Assertions.assertThat(e.extractCronExpression(new StringResource("")).isPresent()).isFalse();
		Assertions.assertThat(e.extractCronExpression(new StringResource("#@Schedule{}")).isPresent()).isTrue();
		Assertions.assertThat(e.extractCronExpression(new StringResource("// #@Schedule{}")).isPresent()).isTrue();
		Assertions.assertThat(e.extractCronExpression(new StringResource("// #@Schedule{\"foo\":\"bar\"}")).get()
				.path("foo").asText()).isEqualTo("bar");
		Assertions
				.assertThat(
						e.extractCronExpression(new StringResource("// #@Schedule{\"invalidJson:\"foo\"}")).isPresent())
				.isFalse();
	}

	
}
