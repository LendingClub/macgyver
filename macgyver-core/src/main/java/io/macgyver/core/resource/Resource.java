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
package io.macgyver.core.resource;

import java.io.IOException;
import java.io.InputStream;


public abstract class Resource {

	ResourceProvider resourceLoader;
	String name;
	public Resource(ResourceProvider loader, String name) {
		this.resourceLoader = loader;
		this.name=name;
	}
	public ResourceProvider getResourceProvider() {
		return resourceLoader;
	}
	public String getPath() throws IOException{
		return name;
	}
	public abstract InputStream openInputStream() throws IOException;
	public abstract boolean exists() ;
	public abstract String getHash() throws IOException;
	public abstract String getContentAsString() throws IOException;
}
