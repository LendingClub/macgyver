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
import static org.junit.Assert.assertEquals;

import java.util.Comparator;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonComparatorsTextTest {
	ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testStringCompare() {
		Comparator<JsonNode> c = JsonNodes.textComparator("test", false);

		assertThat("".compareTo("abc")).isLessThan(0);
		assertThat("a".compareTo("z")).isLessThan(0); // sanity check

		assertThat(
				c.compare(mapper.createObjectNode().put("test", "a"), mapper
						.createObjectNode().put("test", "z"))).isLessThan(0);
		assertThat(
				c.compare(mapper.createObjectNode().put("test", "z"), mapper
						.createObjectNode().put("test", "a"))).isGreaterThan(0);
		assertThat(
				c.compare(mapper.createObjectNode().put("test", "a"), mapper
						.createObjectNode().put("test", "a"))).isEqualTo(0);
		assertThat(
				c.compare(mapper.createObjectNode().put("test", "A"), mapper
						.createObjectNode().put("test", "z"))).isLessThan(0);

		assertThat(
				c.compare(mapper.createObjectNode().put("test", ""), mapper
						.createObjectNode().put("test", "z"))).isLessThan(0);
		assertThat(
				c.compare(mapper.createObjectNode(), mapper.createObjectNode()
						.put("test", "z"))).isLessThan(0);
	}



	@Test
	public void testNumeric() {
		Comparator<JsonNode> c = JsonNodes.textComparator("test");

		assertThat(c.compare(mapper.createObjectNode().put("test", 1), mapper
				.createObjectNode().put("test", 1))).isEqualTo(0);
		assertThat(c.compare(mapper.createObjectNode().put("test", 1),
				mapper.createObjectNode().put("test", 2))).isLessThan(0);
		assertThat(c.compare(mapper.createObjectNode().put("test", 2),
				mapper.createObjectNode().put("test", 1))).isGreaterThan(0);

		assertThat(c.compare(mapper.createObjectNode().put("test", 111),
				mapper.createObjectNode().put("test", 19))).isLessThan(0);

	}

	@Test
	public void testMissingNumeric() {
		Comparator<JsonNode> c = JsonNodes.textComparator("test");

		assertThat(c.compare(mapper.createObjectNode(), mapper
				.createObjectNode().put("test", 19))).isLessThan(0);

		assertThat(c.compare(mapper.createObjectNode().put("test", 19),
				mapper.createObjectNode())).isGreaterThan(0);
	}

	@Test
	public void testNumericAndString() {
		Comparator<JsonNode> c = JsonNodes.textComparator("test");

		assertThat(c.compare(mapper.createObjectNode().put("test", "111"),
				mapper.createObjectNode().put("test", "99"))).isLessThan(0);
		assertThat(c.compare(mapper.createObjectNode().put("test", 111),
				mapper.createObjectNode().put("test", 99))).isLessThan(0);

		assertThat(c.compare(mapper.createObjectNode().put("test", 111),
				mapper.createObjectNode().put("test", "99"))).isLessThan(0);

		assertThat(c.compare(mapper.createObjectNode().put("test", "99"),
				mapper.createObjectNode().put("test", 111))).isGreaterThan(0);

	}

	@Test
	public void testCaseSensitive() {
		Comparator<JsonNode> c = JsonNodes.textComparator("test", true);

		assertThat("a".compareTo("A")).isPositive();
		assertThat(c.compare(mapper.createObjectNode().put("test", "a"),
				mapper.createObjectNode().put("test", "A"))).isPositive();

		assertThat("A".compareTo("a")).isNegative();
		assertThat(c.compare(mapper.createObjectNode().put("test", "A"),
				mapper.createObjectNode().put("test", "a"))).isNegative();

		assertThat("howdy".compareTo("HELLO")).isPositive();
		assertThat(c.compare(
				mapper.createObjectNode().put("test", "howdy"), mapper
						.createObjectNode().put("test", "HELLO"))).isPositive();
	}

	@Test
	public void testCaseInsensitive() {
		Comparator<JsonNode> c = JsonNodes.textComparator("test", false);

		assertEquals(0, "a".compareToIgnoreCase("A"));
		assertEquals(0, c.compare(mapper.createObjectNode().put("test", "a"),
				mapper.createObjectNode().put("test", "A")));

		assertEquals(0, "A".compareToIgnoreCase("a"));
		assertEquals(0, c.compare(mapper.createObjectNode().put("test", "A"),
				mapper.createObjectNode().put("test", "a")));

		assertEquals(0, "hELLo".compareToIgnoreCase("hello"));
		assertEquals(0, c.compare(mapper.createObjectNode()
				.put("test", "hELLo"),
				mapper.createObjectNode().put("test", "hello")));

		assertThat("howdy".compareToIgnoreCase("HELLO")).isPositive();
		assertThat(c.compare(
				mapper.createObjectNode().put("test", "howdy"), mapper
						.createObjectNode().put("test", "HELLO"))).isPositive();
	}

	@Test
	public void testNullEmptyString() {
		Comparator<JsonNode> c = JsonNodes.textComparator("test");

		assertThat(c.compare(mapper.createObjectNode().put("test", "abc"),
				mapper.createObjectNode().put("test", ""))).isPositive();

	}
}
