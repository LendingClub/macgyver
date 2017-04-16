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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.rapidoid.u.U;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import io.macgyver.core.MacGyverException;

public class JsonNodes {

	public static final ObjectMapper mapper = new ObjectMapper();

	public static void sort(List<JsonNode> n, Comparator<JsonNode> comparator) {
		Preconditions.checkNotNull(n);
		Preconditions.checkNotNull(comparator);
		Collections.sort(n, comparator);
	}

	public static void sort(ArrayNode array, Comparator<JsonNode> comparator) {
		Preconditions.checkNotNull(array);
		Preconditions.checkNotNull(comparator);
		List<JsonNode> arr = Lists.newArrayList();
		for (JsonNode n : array) {
			arr.add(n);
		}
		Collections.sort(arr, comparator);
		array.removeAll();
		for (JsonNode n : arr) {
			array.add(n);
		}
	}

	public static class PropertyComparator implements Comparator<JsonNode> {

		String propertyName;
		boolean caseSensitive = true;

		public PropertyComparator(String s, boolean caseSensitive) {
			this.propertyName = s;
			this.caseSensitive = caseSensitive;
		}

		@Override
		public int compare(JsonNode o1, JsonNode o2) {
			if (o1 == o2) {
				return 0;
			}
			if (o1 == null) {
				return -1;
			}
			if (o2 == null) {
				return 1;
			}

			JsonNode n1 = o1.path(propertyName);
			JsonNode n2 = o2.path(propertyName);

			if (n1.isNumber() || n2.isNumber()) {
				if (n1.isNull()) {
					return new Double(0).compareTo(n2.asDouble());
				} else if (n2.isNull()) {
					return new Double(n2.asDouble()).compareTo(0d);
				}
				if (n1.isNumber() && n2.isNumber()) {
					double d1 = n1.asDouble();
					double d2 = n2.asDouble();
					if (d1 < d2) {
						return -1;
					}
					if (d1 > d2) {
						return 1;
					}
					return 0;
				}
			}
			String s1 = n1.asText();
			String s2 = n2.asText();
			if (caseSensitive) {
				return s1.compareTo(s2);
			} else {
				return s1.compareToIgnoreCase(s2);
			}
		}

	}

	public static class TextComparator implements Comparator<JsonNode> {

		String propertyName;
		boolean caseSensitive = true;

		public TextComparator(String s, boolean caseSensitive) {
			this.propertyName = s;
			this.caseSensitive = caseSensitive;
		}

		@Override
		public int compare(JsonNode o1, JsonNode o2) {
			if (o1 == o2) {
				return 0;
			}
			if (o1 == null) {
				return -1;
			}
			if (o2 == null) {
				return 1;
			}

			String s1 = o1.path(propertyName).asText();
			String s2 = o2.path(propertyName).asText();
			if (s1 == null) {
				s1 = "";
			}
			if (s2 == null) {
				s2 = "";
			}
			if (caseSensitive) {
				return s1.compareTo(s2);
			} else {
				return s1.compareToIgnoreCase(s2);
			}
		}

	}

	public static class NumericComparator implements Comparator<JsonNode> {

		String propertyName;
		Number defaultValue = 0;

		public NumericComparator(String s, Number defaultValue) {
			this.propertyName = s;
			if (defaultValue == null) {
				defaultValue = 0;
			} else {
				this.defaultValue = defaultValue;
			}
		}

		@Override
		public int compare(JsonNode o1, JsonNode o2) {
			if (o1 == o2) {
				return 0;
			}
			if (o1 == null) {
				return -1;
			}
			if (o2 == null) {
				return 1;
			}

			Double s1 = o1.path(propertyName).asDouble(defaultValue.doubleValue());
			Double s2 = o2.path(propertyName).asDouble(defaultValue.doubleValue());

			return s1.compareTo(s2);
		}

	}

	public static Comparator<JsonNode> numericComparator(String prop) {
		return new NumericComparator(prop, 0);
	}

	public static Comparator<JsonNode> textComparator(String prop) {
		return new TextComparator(prop, false);
	}

	public static Comparator<JsonNode> textComparator(String prop, boolean caseSenstitive) {

		return new TextComparator(prop, caseSenstitive);

	}

	public static String pretty(JsonNode n) {
		try {
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(n);
		} catch (JsonProcessingException e) {
			throw new MacGyverException(e);
		}
	}

	public static List<JsonNode> arrayToList(JsonNode n, String property) {
		return arrayToList(n.path(property));
	}

	public static List<JsonNode> arrayToList(JsonNode n) {
		if (n.isArray()) {
			return Lists.newArrayList(n.iterator());
		} else {
			return Lists.newArrayList();
		}

	}

	public static ObjectNode createObjectNode(Object... vals) {

		return mapper.convertValue(U.map(vals), ObjectNode.class);

	}
}
