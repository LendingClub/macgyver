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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import com.google.common.base.Preconditions;
import com.google.common.hash.Hashing;

public class StringResource extends Resource {

	String value = null;
	
	public StringResource(String value) {
		this(value,null);
	}
	
	public StringResource(String s, String path) {
		super(null,path);
		Preconditions
		.checkNotNull(s);
		this.value = s;
	}
	
	@Override
	public InputStream openInputStream() throws IOException {
		return new ByteArrayInputStream(value.getBytes());
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public String getHash() throws IOException {
		return Hashing.sha1().hashString(value, Charset.defaultCharset()).toString();
	}

	@Override
	public String getContentAsString() throws IOException {
		return value;
	}

	

}
