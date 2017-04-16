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

import io.macgyver.okrest3.OkRestTarget;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public abstract class AbstractSearchBuilder<T> {

	OkRestTarget target;

	List<String> repoList = Lists.newArrayList();
	boolean detailProperties = true;
	boolean detailInfo = true;
	public String path;
	
	public AbstractSearchBuilder(OkRestTarget r, String p) {
		Preconditions.checkNotNull(r);
		this.target = r;
		this.path = p;
	}



	public T inAllRepos() {
		this.repoList.clear();
		return (T) this;
	}

	public T inRepo(String name) {
		Preconditions.checkNotNull(name);
		this.repoList.add(name);
		return (T) this;
	}

	public T inRepos(String... names) {
		for (String n : names) {
			this.repoList.add(n);
		}
		return (T) this;
	}

	public T withProperties(boolean b) {
		this.detailProperties = b;
		return (T) this;
	}

	public T withInfo(boolean b) {
		this.detailInfo = b;
		return (T) this;
	}

	public void formatRequest() throws IOException {
		String xResultDetail = "";
		if (detailInfo) {
			xResultDetail = "info";
		}
		if (detailProperties) {
			if (!Strings.isNullOrEmpty(xResultDetail)) {
				xResultDetail = xResultDetail + ", ";
			}
			xResultDetail = xResultDetail + "properties";

		}
		if (!com.google.common.base.Strings.isNullOrEmpty(xResultDetail)) {
			target = target.header("X-Result-Detail", xResultDetail);
		}
		if (!repoList.isEmpty()) {
			target = target.queryParam("repos", Joiner.on(",").skipNulls()
					.join(repoList));
		}	
	}
	
	public JsonNode execute() throws IOException {
		
		formatRequest();
		OkRestTarget t = target.path(path);

		return t.get().execute(JsonNode.class);
	}
}
