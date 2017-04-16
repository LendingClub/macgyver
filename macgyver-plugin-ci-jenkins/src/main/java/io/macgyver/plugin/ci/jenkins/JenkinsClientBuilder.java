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
package io.macgyver.plugin.ci.jenkins;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public class JenkinsClientBuilder {

	
	private String url;
	private String username;
	private String password;
	
	public JenkinsClientBuilder() {
		
	}
	

	public JenkinsClientBuilder url(String url) {
		this.url = url;
		return this;
	}
	
	public JenkinsClientBuilder credentials(String username, String password) {
		this.username=username;
		this.password=password;
		return this;
	}
	
	public JenkinsClient build() {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(url));
		Preconditions.checkArgument(url.toLowerCase().startsWith("http://") || url.toLowerCase().startsWith("https://"), "url must be http or https");
		return new JenkinsClientImpl(url, username, password);
	}
}
