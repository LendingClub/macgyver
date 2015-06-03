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

import java.io.IOException;

import io.macgyver.okrest.OkRestTarget;

public class PropertySearchBuilder extends AbstractSearchBuilder<PropertySearchBuilder> {


	public PropertySearchBuilder(OkRestTarget target) {
		super(target,"/api/search/prop");
	}
	
	public PropertySearchBuilder property(String key, String val) {
		target = target.queryParameter(key, val);
		return this;
	}

	@Override
	public void formatRequest() throws IOException {
		
		super.formatRequest();
	}


}
