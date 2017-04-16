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
package io.macgyver.plugin.atlassian.confluence;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.macgyver.core.MacGyverException;

public class ContentRequestBuilder {

	Logger logger = LoggerFactory.getLogger(ContentRequestBuilder.class);
	
	ObjectMapper mapper = new ObjectMapper();
	ConfluenceClient client;

	String id;
	String content;
	String representation = "storage";
	String title;
	String expand="body.storage,version";
	String type = "page";
	String status = "current";

	public ContentRequestBuilder(ConfluenceClient c) {
		this.client = c;
	}

	public ContentRequestBuilder content(String content) {
		this.content = content;
		return this;
	}

	public ContentRequestBuilder id(String id) {
		this.id = id;
		return this;
	}

	ObjectNode createUpdateRequest(JsonNode n) {
		ObjectNode request = mapper.createObjectNode();
		request.put("id", n.get("id").asText());
		request.put("type", n.get("type").asText());
		request.put("title", n.get("title").asText());

		ObjectNode version = mapper.createObjectNode();
		version.put("number", n.get("version").get("number").asInt() + 1);
		version.put("minorEdit", false);
		request.set("version", version);

		ObjectNode body = mapper.createObjectNode();
		ObjectNode storage = mapper.createObjectNode();
		body.set("storage", storage);
		storage.put("value", n.get("body").get("storage").get("value").asText());
		storage.put("representation",
				n.get("body").get("storage").get("representation").asText());
		request.set("body", body);

		return request;
	}

	public JsonNode update() {

		try {
			if (id != null) {
				JsonNode existing = get(id);

				ObjectNode updateRequest = createUpdateRequest(existing);

				((ObjectNode) updateRequest.path("body").path("storage")).put(
						"value", content);

				if (title!=null) {
					((ObjectNode) updateRequest).put(
							"title", title);
				}
				
				
				io.macgyver.okrest3.OkRestResponse response = client.getBaseTarget()
						.path("content").path(id)
						.contentType("application/json").put(updateRequest)
						.execute();

				if (response.response().isSuccessful()) {
					return response.getBody(JsonNode.class);
				} else {
					String value = response.response().body().string();
					throw new MacGyverException(value);
				}

			} else {
				throw new IllegalArgumentException();
			}

		} catch (IOException e) {
			throw new MacGyverException(e);
		}
	}

	public ContentRequestBuilder title(String s) {
		this.title = s;
		return this;
	}
	
	public JsonNode search() {
		if (title != null) {
			return client.getBaseTarget().path("/content")
					.queryParam("title", title)
					.contentType("application/json")
					.queryParam("expand", expand).get()
					.execute(JsonNode.class);
		}
		throw new IllegalArgumentException();
	}

	public JsonNode get(String contentId) {
		id(contentId);
		if (id != null) {
			return client.getBaseTarget().path("/content").path(id)
					.contentType("application/json")
					.queryParam("expand", expand).get()
					.execute(JsonNode.class);
		} else {
			throw new IllegalArgumentException();
		}
	}
}
