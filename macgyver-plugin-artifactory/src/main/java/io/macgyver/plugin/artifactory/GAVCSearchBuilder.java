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

public class GAVCSearchBuilder extends AbstractSearchBuilder<GAVCSearchBuilder>  {



	public GAVCSearchBuilder(OkRestTarget r) {
		super(r,"/api/search/gavc");
	}

	public GAVCSearchBuilder group(String group) {
		target = target.queryParameter("g", group);
		return this;
	}

	public GAVCSearchBuilder artifact(String artifact) {
		target = target.queryParameter("a", artifact);
		return this;
	}

	public GAVCSearchBuilder version(String version) {
		target = target.queryParameter("v", version);
		return this;
	}

	public GAVCSearchBuilder classifier(String classifier) {
		target = target.queryParameter("c", classifier);
		return this;
	}

	@Override
	public void formatRequest() throws IOException {
		
		super.formatRequest();
	}



}
