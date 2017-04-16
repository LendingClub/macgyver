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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import rx.functions.Func1;

public class Neo4jPropertyFlattener implements Func1<JsonNode, ObjectNode>{

	static ObjectMapper mapper = new ObjectMapper();



	ObjectNode flattenx(JsonNode jsonNode) {
		return flatten((ObjectNode) jsonNode, mapper.createObjectNode(), null);
	}

	ObjectNode flatten(JsonNode input, ObjectNode output, String context) {
		Preconditions.checkNotNull(input);
		Preconditions.checkNotNull(output);

		input.fields().forEachRemaining(it -> {

			if (it.getValue().isContainerNode()) {
				String newContext = context;
				if (Strings.isNullOrEmpty(context)) {
					newContext = it.getKey();
				} else {
					newContext = context + "_" + it.getKey();
				}
				if (it.getValue().isArray()) {
					ArrayNode na = JsonNodes.mapper.createArrayNode();
					AtomicBoolean consistent = new AtomicBoolean(true);
					AtomicReference<JsonNodeType> nodeType = new AtomicReference<>();
					it.getValue().elements().forEachRemaining(it2 -> {
						// not really a way to handle arrays of complex objects
						// neo4j does not accept mixed types in arrays either,
						// so we need to go back and coerce
						// if they are mixed
						na.add(it2);
						
					
						if (!consistent.get()) {
							// no more checking required
						}
						else if (nodeType.get() != null && !nodeType.get().equals(it2.getNodeType())) {
				
							consistent.set(false);
						}
						nodeType.set(it2.getNodeType());

					});
					if (!consistent.get()) {
						ArrayNode na2 = JsonNodes.mapper.createArrayNode();
						na.iterator().forEachRemaining(itx -> {
							na2.add(itx.asText());
						});
						output.set(sanitizeKey(newContext), na2);
					}
					else {
						output.set(sanitizeKey(newContext), na);
					}
					
				} else {
					flatten(it.getValue(), output, newContext);
				}
			} else {
				String pname = null;
				if (Strings.isNullOrEmpty(context)) {
					pname = it.getKey();
				} else {
					pname = Strings.nullToEmpty(context) + "_" + it.getKey();
				}
				output.set(sanitizeKey(pname), it.getValue());
			}
		});
		return output;
	}

	protected String sanitizeKey(String key) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < key.length(); i++) {
			char c = key.charAt(i);
			if (Character.isLetterOrDigit(c) || c == '_') {
				sb.append(c);
			} else {
				sb.append("_");
			}
		}
		return sb.toString();
	}

	@Override
	public ObjectNode call(JsonNode t) {
		return flatten(t, mapper.createObjectNode(), null);
	}
}
