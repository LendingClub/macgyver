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

import java.util.concurrent.TimeUnit;

import io.macgyver.okrest.BasicAuthInterceptor;
import io.macgyver.okrest.OkRestClient;
import io.macgyver.okrest.OkRestTarget;

import com.squareup.okhttp.OkHttpClient;

public class BasicConfluenceClient implements ConfluenceClient {

	private OkRestTarget target;
	
	public BasicConfluenceClient(String url, String username, String password) {
		OkHttpClient c = new OkHttpClient();
		c.interceptors().add(new BasicAuthInterceptor(username, password));
		
		OkRestClient client = new OkRestClient(c);
		
		this.target = client.uri(url);
		
	//	client.getConverterRegistry().setDefaultResponseErrorHandler(new ConfluenceResponseErrorHandler());
		
	}

	@Override
	public OkRestTarget getBaseTarget() {
		return target;
	}
	
	public ContentRequestBuilder contentRequest() {
		return new ContentRequestBuilder(this);
	}
}
