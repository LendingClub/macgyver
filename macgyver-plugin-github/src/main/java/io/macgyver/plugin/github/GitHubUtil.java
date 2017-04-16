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
package io.macgyver.plugin.github;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.kohsuke.github.GHRepository;

import com.google.common.base.Preconditions;

public class GitHubUtil {

	/**
	 * github-api does not provide a way to set the default branch until 1.69.  
	 * GHRepository.edit() can do it, but is private.  This is a simple workaround
	 * that uses reflection to work around the private limitation.
	 * 
	 * @param repo
	 * @param branchName
	 * @throws IOException
	 */
	public static void setDefaultBranch(GHRepository repo, String branchName)
			throws IOException {
		Preconditions.checkNotNull(repo);
		Preconditions.checkNotNull(branchName);

		try {
			Method editMethod = repo.getClass().getDeclaredMethod("edit",
					String.class, String.class);
			editMethod.setAccessible(true);

			editMethod.invoke(repo, "default_branch", branchName);
		} catch (IllegalAccessException | InvocationTargetException
				| NoSuchMethodException e) {
			throw new IOException(e);
		}

	}
}
