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
package io.macgyver.core.resource.provider.filesystem;

import io.macgyver.core.resource.Resource;
import io.macgyver.core.resource.ResourceProvider;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.hash.Hashing;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;

public class FileSystemResource extends Resource {

	File fileObject;

	public FileSystemResource(ResourceProvider loader, String name, File object) {
		super(loader, name);
		this.fileObject = object;
	}

	@Override
	public String toString() {
		try {
			return MoreObjects.toStringHelper(this).add("path", getPath())
					.toString();
		} catch (Exception e) {
			return MoreObjects.toStringHelper(this).toString();
		}
	}

	@Override
	public String getHash() throws IOException {
		return Files.hash(fileObject, Hashing.sha1()).toString();

	}

	@Override
	public String getContentAsString() throws IOException {
		String text;
		try (InputStreamReader reader = new InputStreamReader(
				openInputStream(), Charsets.UTF_8)) {
			text = CharStreams.toString(reader);
		}
		return text;
	}

	@Override
	public InputStream openInputStream() throws IOException {
		return new BufferedInputStream(new FileInputStream(fileObject));
	}

	@Override
	public boolean exists() {
		return fileObject.exists();
	}
}
