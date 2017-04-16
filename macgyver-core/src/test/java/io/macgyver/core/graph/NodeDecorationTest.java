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
package io.macgyver.core.graph;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import rx.Observable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NodeDecorationTest {

	
	@Test
	public void testIt() {
		
		final JsonNode n = new ObjectMapper().createObjectNode();
		
		NodeInfo x = new NodeInfo<Object>(99, n, null, this);
		final NodeDecorationTest outer = this;
		NodeInfo.Action action = new NodeInfo.Action() {

			@Override
			public void call(NodeInfo decoration) {
	
				assertThat(decoration).isNotNull();
				assertThat(decoration.getNodeId()).isEqualTo(99);
				assertThat(decoration.getNode()).isSameAs(n);
				assertThat(decoration.getUserData()).isSameAs(outer);
				
			}
		};
		
	
		Observable.just(x).subscribe(action);
		
	
		

	}
}
