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

import org.lendingclub.neorx.NeoRxClient;

import com.fasterxml.jackson.databind.JsonNode;

import rx.functions.Action1;

public class NodeInfo<T extends Object> {

	public static interface Action extends Action1<NodeInfo> {
		
	};
	long nodeId;
	JsonNode node;
	T userData;
	NeoRxClient neoRxClient;
	
	public NodeInfo(long id, JsonNode n, NeoRxClient client, T userData) {
		this.nodeId = id;
		this.node = n;
		this.userData = userData;
		this.neoRxClient = client;
	}

	public long getNodeId() {
		return nodeId;
	}

	public JsonNode getNode() {
		return node;
	}

	public T getUserData() {
		return userData;
	}

	public NeoRxClient getNeoRxClient() {
		return neoRxClient;
	}
	
	
}
