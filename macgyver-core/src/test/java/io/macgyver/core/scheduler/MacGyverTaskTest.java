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

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class MacGyverTaskTest {

	@Test
	public void testIt() {
		
		ObjectNode params = new ObjectMapper()
		.createObjectNode().put("foo", "bar").put("intval", 2)
		.put("boolval", true);
		
		params.set("objectNode", new ObjectMapper().createObjectNode());
		
		params.set("arrayNode", new ObjectMapper().createObjectNode());
		
		ObjectNode n = new ObjectMapper().createObjectNode();
		n.set("parameters", params);
		
		MacGyverTask task = new MacGyverTask(n);

		Map<String, Object> m = task.createArgsFromConfig();

		Assertions.assertThat(m).hasSize(3).containsEntry("foo", "bar")
				.containsEntry("intval", "2").containsEntry("boolval", "true");

	}
}
