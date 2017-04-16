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
package io.macgyver.core.service.config;

import static io.macgyver.core.util.MacGyverObjectMapper.objectMapper;

import java.io.IOException;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.beust.jcommander.internal.Maps;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.macgyver.core.util.MacGyverObjectMapper;
import io.macgyver.test.MacGyverIntegrationTest;

public class HJsonConfigLoaderTest extends MacGyverIntegrationTest {

	
	@Test
	public void testProcess()throws IOException,JsonProcessingException {
		
		
		JsonNode n = objectMapper.readTree(getClass().getResource("/test-services.json"));
		
		HJsonConfigLoader cl = new HJsonConfigLoader();
		
		
		Assertions.assertThat( cl.process(n, null)).containsEntry("myservice.fizz", "buzz").containsEntry("myservice.foo","x");
		Assertions.assertThat( cl.process(n, "invalid")).containsEntry("myservice.fizz", "buzz").containsEntry("myservice.foo","x");
		Assertions.assertThat( cl.process(n, "qa")).containsEntry("myservice.fizz", "buzz").containsEntry("myservice.foo","qafoo");

	}
	
	@Test
	public void testApply()throws IOException,JsonProcessingException {
		
		
		HJsonConfigLoader cl = new HJsonConfigLoader();
		
		Map<String,String> x = Maps.newHashMap();
		cl.apply(x);
		
		Assertions.assertThat(x).containsEntry("foo.a", "1");

	}
}
