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
package io.macgyver.plugin.atlassian.jira;

import io.macgyver.okrest.BasicAuthInterceptor;
import io.macgyver.okrest.OkRestClient;
import io.macgyver.okrest.OkRestTarget;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.squareup.okhttp.OkHttpClient;

public class JiraClientImpl implements JiraClient {

	private OkRestTarget target;
	
	public JiraClientImpl(String url, String username, String password) {
		
		OkHttpClient c = new OkHttpClient();
		c.interceptors().add(new BasicAuthInterceptor(username, password));
		
		OkRestClient client = new OkRestClient(c);
		target = client.url(url);
		
	}
	@Override
	public JsonNode getIssue(String issue) throws IOException {
		return target.path("issue").path(issue).get().execute(JsonNode.class);
	}

	@Override
	public JsonNode getJson(String path) throws IOException {
		return target.path(path).get().execute(JsonNode.class);
	}

	@Override
	public JsonNode postJson(String path, JsonNode body) throws IOException{
		return target.path(path).post(body).execute(JsonNode.class);
	}
	
	
	@Override
	public OkRestTarget getOkRestTarget() {
		return target;
	}

}
