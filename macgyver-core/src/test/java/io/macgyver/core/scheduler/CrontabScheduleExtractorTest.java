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

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.macgyver.core.resource.StringResource;

public class CrontabScheduleExtractorTest {
	private StringResource script = new StringResource(
			"#@Schedule{\"n\": 1, \"env\": \"prod\"}\n"
			+ "#@Schedule {\"n\": 3, \"env\": \"demo\"}\n"
			+ "#@Schedule {\"n\": 2}\n");
	
	@Test
	public void testProd() {
		ObjectNode o = new CrontabExpressionExtractor().withProfile("prod_nevada").extractCronExpression(script).get();
		Assertions.assertThat(o.path("n").asInt()).isEqualTo(1);
	}
	
	@Test
	public void testDev() {
		ObjectNode o = new CrontabExpressionExtractor().withProfile("lcdev").extractCronExpression(script).get();
		Assertions.assertThat(o.path("n").asInt()).isEqualTo(2);
	}
	
	@Test
	public void testDefault() {
		ObjectNode o = new CrontabExpressionExtractor().extractCronExpression(script).get();
		Assertions.assertThat(o.path("n").asInt()).isEqualTo(2);
	}
	
	@Test
	public void testDemo() {
		ObjectNode o = new CrontabExpressionExtractor().withProfile("demo").extractCronExpression(script).get();
		Assertions.assertThat(o.path("n").asInt()).isEqualTo(3);
	}

}
