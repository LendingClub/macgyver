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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


public abstract class ServiceCatalogMessage extends io.macgyver.core.event.MacGyverMessage {

	public static class UpdateMessage extends ServiceCatalogMessage {

		public UpdateMessage withCatalogEntry(ObjectNode n) {

			ObjectNode x = (ObjectNode) getData();
			x.set("entry", n);
			return this;
		}

		public UpdateMessage withCatalogEntrySource(ObjectNode n) {
			ObjectNode x = (ObjectNode) getData();

			x.set("entrySource", n);
			return this;
		}

	}

	public static class ErrorMessage extends ServiceCatalogMessage {

		
		
		public String getErrorMessage() {
			return getData().path("errorMessage").asText();
		}
		
		public ErrorMessage withErrorMessage(String s) {
			ObjectNode x = (ObjectNode) getData();

			x.put("errorMessage", s);
			return this;
		}

		public ErrorMessage withEntryType(String s) {
			ObjectNode x = (ObjectNode) getData();

			x.put("entryType", s);
			return this;
		}
		public ErrorMessage withId(String id) {
			ObjectNode x = (ObjectNode) getData();

			x.put("id", id);
			return this;
		}

	}

}
