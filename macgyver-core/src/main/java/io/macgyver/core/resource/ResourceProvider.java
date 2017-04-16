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

import com.google.common.base.Optional;
import com.google.common.base.Strings;

import rx.Observable;

public abstract class ResourceProvider {

	String prefix=null;
	
	
	public abstract Iterable<Resource> findResources(ResourceMatcher rm) throws IOException;
	public final Iterable<Resource> findResources() throws IOException {
		return findResources(ResourceMatchers.matchAll());
	}
	
	public Observable<Resource> allResources() throws IOException {
		return Observable.from(findResources());
	}
	
	public Optional<Resource> findResourceByHash(String hash) throws IOException {
		for (Resource r: findResources()) {
			if (r.getHash().equals(hash)) {
				return Optional.fromNullable(r);
			}
		}
		return Optional.absent();
	}
	public Optional<Resource> findResourceByPath(String path) throws IOException {
		try {
			return Optional.fromNullable(getResourceByPath(path));
		}
		catch (IOException e) {
			
		}
		return Optional.absent();
	}
	
	public abstract Resource getResourceByPath(String path) throws IOException;
	
	
	public String removePrefix(String input) {
		if (input==null || prefix==null) {
			return input;
		}
		if (input.startsWith(prefix+"/")) {
			return input.substring(prefix.length()+1);
		}
		return input;
	}
	public String addPrefix(String input) {
		if (Strings.isNullOrEmpty(prefix)) {
			return input;
		}
		else {
			return prefix+"/"+input;
		}
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public String getPrefix() {
		return prefix;
	}
	
	public abstract void refresh() throws IOException;
}
