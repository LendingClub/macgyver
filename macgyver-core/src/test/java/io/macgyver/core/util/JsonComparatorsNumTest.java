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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonComparatorsNumTest {
	ObjectMapper mapper = new ObjectMapper();

	
	void assertCorrect(Double a, Double b) {
		int expectedValue = a.compareTo(b);
		Comparator<JsonNode> c = JsonNodes.numericComparator("test");

		int actualValue = c.compare(mapper.createObjectNode().put("test", a),mapper.createObjectNode().put("test",b));
		
		 if (expectedValue<0) {
			 assertThat(actualValue).isLessThan(0);
			
		}
		else if (expectedValue>0) {
			 assertThat(actualValue).isGreaterThan(0);
		}
		else {
			 assertThat(actualValue).isEqualTo(0);
		}
		
	}
	
	@Test
	public void testIt() {
		assertCorrect(1d,2d);
		assertCorrect(2d,1d);
		assertCorrect(99d,99d);
	}
	
	@Test
	public void testStringConversion() {
		Comparator<JsonNode> c = JsonNodes.numericComparator("test");

		Assertions.assertThat(c.compare(mapper.createObjectNode().put("test", "1"),mapper.createObjectNode().put("test","2"))).isLessThan(0);
		Assertions.assertThat(c.compare(mapper.createObjectNode().put("test", "2"),mapper.createObjectNode().put("test","1"))).isGreaterThan(0);
		
		Assertions.assertThat(c.compare(mapper.createObjectNode().put("test", "1"),mapper.createObjectNode().put("test",2))).isLessThan(0);
		Assertions.assertThat(c.compare(mapper.createObjectNode().put("test", 2),mapper.createObjectNode().put("test","1"))).isGreaterThan(0);
		
		Assertions.assertThat(c.compare(mapper.createObjectNode(),mapper.createObjectNode().put("test",2))).isLessThan(0);
		Assertions.assertThat(c.compare(mapper.createObjectNode().put("test", 2),mapper.createObjectNode())).isGreaterThan(0);
		
		Assertions.assertThat(c.compare(mapper.createObjectNode().put("test", "garbage"),mapper.createObjectNode().put("test","2"))).isLessThan(0);
		Assertions.assertThat(c.compare(mapper.createObjectNode().put("test", "1"),mapper.createObjectNode().put("test","garbage"))).isGreaterThan(0);
		
		
	}
}
