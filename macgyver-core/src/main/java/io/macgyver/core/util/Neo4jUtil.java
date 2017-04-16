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

import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;

public class Neo4jUtil {

	public static ObjectNode scrubNonCompliantNeo4jAttributes(ObjectNode t) {
		ObjectNode n = t.deepCopy();
		
		for (String name : Lists.newArrayList(n.fieldNames())) {

			JsonNode val = n.get(name);
			if (val == null) {
				// shouldn't happen
			} else if (val.isValueNode()) {
				// simple value nodes are OK
			} else if (val.isArray()) {
				ArrayNode an = (ArrayNode) val;
				an.forEach(cn -> {
					if (cn!=null && cn.isContainerNode() ) {
						n.remove(name);
					}
					// note that types of the array have to be consistent.  Neo4j does not support [ 123, "abc" ]
					// But let that be the responsibility of the caller
				});

				// neo4j does not support nested objects
			}
			else if (val.isObject()) {
				n.remove(name);
			}
			else if (val.isNull()) {
				
			}
			else if (val.isMissingNode()) {
				n.remove(name);
			}

		}

		return n;
	}

}
