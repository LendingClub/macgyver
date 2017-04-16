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
package io.macgyver.core.cluster;

import io.macgyver.test.MacGyverIntegrationTest;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.assertj.core.internal.Comparables;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;

public class ClusterManagerTest extends MacGyverIntegrationTest {

	ObjectMapper mapper = new ObjectMapper();
	
	@Autowired
	ClusterManager clusterManager;

	Comparator<JsonNode> c = ClusterManager.clusterNodeComparator();
	@Test
	public void testA() {

			
		ObjectNode a = mapper.createObjectNode().put("id", "a");
		ObjectNode b = mapper.createObjectNode().put("id", "b");	
		Assertions.assertThat(c.compare(a, b)).isEqualTo(-1);
		List<JsonNode> x = Lists.newArrayList(b,a);
		Collections.sort(x,c);
		Assertions.assertThat(x.get(0)).isSameAs(a);
		
	}
	@Test
	public void testPriorityPrcedence() {

			
		ObjectNode a = mapper.createObjectNode().put("id", "a").put("priority", 0);
		ObjectNode b = mapper.createObjectNode().put("id", "z").put("priority", 1);
		Assertions.assertThat(c.compare(a, b)).isEqualTo(1);
		
	}
		
	@Test
	public void testNonNumericPriorty() {

			
		ObjectNode a = mapper.createObjectNode().put("id", "a").put("priority", "0");
		ObjectNode b = mapper.createObjectNode().put("id", "z").put("priority", "1");
		Assertions.assertThat(c.compare(a, b)).isEqualTo(1);
		
	}
}
