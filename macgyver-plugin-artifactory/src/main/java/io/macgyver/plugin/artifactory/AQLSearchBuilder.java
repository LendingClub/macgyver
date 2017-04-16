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
package io.macgyver.plugin.artifactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;

import io.macgyver.okrest3.OkRestTarget;

public class AQLSearchBuilder {

	OkRestTarget target;
	String aql;
	
	AQLSearchBuilder(OkRestTarget t) {
		this.target = t;
	}
	public AQLSearchBuilder aql(String aql) {
		this.aql = aql;
		return this;
	}
	
	public JsonNode execute() {
		Preconditions.checkState(target!=null, "OkRestTarget not set");
		return target.path("api/search/aql").addHeader("Content-type", "text/plain").post(aql).execute(JsonNode.class);
	}
}
