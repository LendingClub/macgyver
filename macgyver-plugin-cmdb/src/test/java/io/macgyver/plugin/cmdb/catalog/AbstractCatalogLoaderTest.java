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
package io.macgyver.plugin.cmdb.catalog;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonNull;

import io.macgyver.core.util.Neo4jUtil;

public class AbstractCatalogLoaderTest {

	ObjectMapper mapper = new ObjectMapper();
	Logger logger = LoggerFactory.getLogger(AbstractCatalogLoader.class);
	
	@Test
	public void testIt() {
		
		ObjectNode n = mapper.createObjectNode();
		
		n.put("a", "1");
		n.put("b", (String) null);
		ArrayNode an = mapper.createArrayNode();
		an.add("a");
		an.add("b");
		an.add(mapper.createObjectNode());
		n.set("c", an);
		
		n.set("d", mapper.createObjectNode());
		
		n.set("e", MissingNode.getInstance());
		n.set("f", NullNode.getInstance());
		
		an = mapper.createArrayNode();
		an.add("foo");
		n.set("g", an);
		
		ObjectNode copy = Neo4jUtil.scrubNonCompliantNeo4jAttributes(n);
		
		logger.info("source: {}",n);
		logger.info("scrubbed: {}",copy);
		
		Assertions.assertThat(copy).isNotSameAs(n);
		Assertions.assertThat(copy.get("a").asText()).isEqualTo("1");
		
		Assertions.assertThat(copy.get("g").isArray()).isTrue();
		
		Assertions.assertThat(copy.get("a").asText()).isEqualTo("1");
		Assertions.assertThat(copy.get("b").isNull()).isTrue();
		Assertions.assertThat(copy.has("c")).isFalse();
		Assertions.assertThat(copy.has("d")).isFalse();
		Assertions.assertThat(copy.has("e")).isFalse();
		
		
	}
}
