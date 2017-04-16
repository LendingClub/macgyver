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

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Neo4jPropertyFlattenerTest {

	Logger logger = LoggerFactory.getLogger(Neo4jPropertyFlattenerTest.class);
	
	String example = "\n" + 
			"{\n" + 
			"  \"host\" : \"myhost01\",\n" + 
			"  \"app\" : {\n" + 
			"    \"scm_branch\" : \"mybranch\",\n" + 
			"    \"scm_revision\" : \"abc123\",\n" + 
			"    \"profile\" : \"stage\",\n" + 
			"    \"artifactId\" : \"myapp\",\n" + 
			"    \"groupId\" : \"com.example\",\n" + 
			"    \"version\" : \"1.2\",\n" + 
			"    \"buildTime\" : \"Fri Oct 09 15:20:33 PDT 2015\",\n" + 
			"    \"allHttpPorts\" : [ 8080 ],\n" + 
			"    \"httpPort\" : 8080,\n" + 
			"    \"contextPath\" : \"\",\n" + 
			"    \"status\" : \"OK\"\n" + 
			"  },\n" + 
			"  \"jvm\" : {\n" + 
			"    \"vmName\" : \"Java HotSpot(TM) 64-Bit Server VM\",\n" + 
			"    \"vmVersion\" : \"25.60-b23\",\n" + 
			"    \"inputArguments\" : [ \"-Dnop=bar\"],\n" + 
			"    \"Code_Cache_max\" : 251658240,\n" + 
			"    \"Metaspace_max\" : -1,\n" + 
			"    \"Compressed_Class_Space_max\" : 1073741824,\n" + 
			"    \"Par_Eden_Space_max\" : 139591680,\n" + 
			"    \"Par_Survivor_Space_max\" : 17432576,\n" + 
			"    \"CMS_Old_Gen_max\" : 2446983168\n" + 
			"  },\n" + 
			"  \"headers\" : {\n" + 
			"    \"User-Agent\" : \"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36\"\n" + 
			"  }\n" + 
			"}";
	
	@Test
	public void testCheckIn() throws IOException {
		JsonNode n = JsonNodes.mapper.readTree(example);
		
		System.out.println(JsonNodes.pretty(new Neo4jPropertyFlattener().call(n)));
	}
	@Test
	public void testIt() {
		ObjectNode n = JsonNodes.mapper.createObjectNode();
		
		Neo4jPropertyFlattener pf = new Neo4jPropertyFlattener();
		n.put("a", 1);
		n.put("b", 2);
		n.put("foo-bar", "baz");
		n.put("fu.bar", "foobar");
		n.set("sub", JsonNodes.mapper.createObjectNode().put("x", 3).put("y", 4));
		n.set("player", JsonNodes.mapper.createObjectNode().put("name", "Jerry Garcia"));
		n.set("n1", JsonNodes.mapper.createObjectNode().set("n2", JsonNodes.mapper.createObjectNode().put("abc", 123)));
		n.set("mixedType", JsonNodes.mapper.createArrayNode().add("10").add(10));
		n.set("singleType", JsonNodes.mapper.createArrayNode().add(20).add(10));
		ObjectNode out = pf.call(n);
		
		Assertions.assertThat(out.path("a").asInt()).isEqualTo(1);
		Assertions.assertThat(out.path("b").asInt()).isEqualTo(2);
		Assertions.assertThat(out.path("sub_x").asInt()).isEqualTo(3);
		Assertions.assertThat(out.path("sub_y").asInt()).isEqualTo(4);
		
		Assertions.assertThat(out.path("mixedType").get(0).isTextual()).isTrue();
		Assertions.assertThat(out.path("mixedType").get(1).isTextual()).isTrue();

		Assertions.assertThat(out.path("singleType").get(0).isNumber()).isTrue();
		Assertions.assertThat(out.path("singleType").get(1).isNumber()).isTrue();
		
		
		Assertions.assertThat(out.path("foo_bar").asText()).isEqualTo("baz");
		Assertions.assertThat(out.path("fu_bar").asText()).isEqualTo("foobar");
		Assertions.assertThat(out.path("player_name").asText()).isEqualTo("Jerry Garcia");
		logger.info(JsonNodes.pretty(out));
		
	}

}
