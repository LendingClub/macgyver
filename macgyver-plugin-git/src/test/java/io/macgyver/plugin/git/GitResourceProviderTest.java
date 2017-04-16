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

import java.io.File;
import java.io.IOException;
import java.util.Map;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.assertj.core.api.Assertions;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.io.Files;

import io.macgyver.core.resource.Resource;
import io.macgyver.core.resource.ResourceMatcher;
import io.macgyver.core.resource.ResourceMatchers;
import io.macgyver.test.MacGyverIntegrationTest;

public class GitResourceProviderTest extends MacGyverIntegrationTest {

	Logger logger = LoggerFactory.getLogger(GitRepositoryServiceFactory.class);
	static File repoDir = null;
	static GitResourceProvider provider;

	@BeforeClass
	public static void setupTest() throws ZipException, IOException {
		File dir = Files.createTempDir();
		File zip = new File("./src/test/resources/test-repo.zip");

		ZipFile zf = new ZipFile(zip);
		zf.extractAll(dir.getAbsolutePath());
		repoDir = dir;

		provider = new GitResourceProvider(repoDir.toURI().toURL().toString(),"","");

	}

	@AfterClass
	public static void cleanup() {
		provider.close();
	}

	@Test(expected = IOException.class)
	public void testInvalidRef() throws IOException {

		provider.setGitRef("abcdef12345");
		provider.findResources();

	}

	@Test
	public void testPathNotFound() throws IOException {

		provider.setGitRef("refs/heads/master");
		try {
			provider.getResourceByPath("does/not/exist");
			Assert.fail();
		} catch (IOException e) {

		}
	}

	@Test
	public void testX() throws IOException {
		Assume.assumeFalse(isRunningInCircleCI());
		GitResourceProvider rp = new GitResourceProvider("https://github.com/if6was9/macgyver-resource-test.git");
		
		Map<String,Resource> m = Maps.newHashMap();
		rp.allResources().forEach(it -> {
			try {
				logger.info("found resource: {}",it);
				m.put(it.getPath(), it);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		} );

		Assertions.assertThat(m).containsKey("scripts/hello.groovy");
	}

	@Test
	@Ignore
	public void testClone() throws GitAPIException, IOException {
		Assume.assumeFalse(isRunningInCircleCI());
		GitResourceProvider p = new GitResourceProvider(
				"https://github.com/if6was9/macgyver-resource-test.git");

		p.setGitRef("7e0ad83ff14d");

		Assert.assertNotNull(p.getResourceByPath("scripts/another.groovy"));
		Assert.assertNotNull(p.getResourceByPath("scripts/hello.groovy"));
		Assert.assertTrue(p.getResourceByPath("scripts/test/test.txt")
				.getContentAsString().startsWith("abc123"));

	}

}
