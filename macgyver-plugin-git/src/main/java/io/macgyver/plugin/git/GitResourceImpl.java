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
package io.macgyver.plugin.git;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.io.CharStreams;

import io.macgyver.core.resource.Resource;
import io.macgyver.core.resource.ResourceProvider;

public class GitResourceImpl extends Resource {

	ObjectId id;

	public GitResourceImpl(ResourceProvider loader, ObjectId objectId,
			String name) {
		super(loader, name);
		this.id = objectId;


	}

	protected GitResourceProvider getGitResourceProvider() {
		return (GitResourceProvider) getResourceProvider();
	}

	@Override
	public InputStream openInputStream() throws IOException {
		GitResourceProvider p = getGitResourceProvider();
		ObjectLoader loader = p.repo.open(id, Constants.OBJ_BLOB);
		return loader.openStream();

	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public String getHash() throws IOException {

		return id.getName();
	}

	@Override
	public String getContentAsString() throws IOException {
		try (InputStreamReader reader = new InputStreamReader(
				openInputStream(), Charsets.UTF_8)) {
			String text = CharStreams.toString(reader);
			return text;
		}
	}

	public String toString() {
		try {
			return MoreObjects.toStringHelper(this).add("objectId", id)
					.add("name", getPath()).toString();
		} catch (Exception e) {
			return MoreObjects.toStringHelper(this).toString();
		}
	}
}
