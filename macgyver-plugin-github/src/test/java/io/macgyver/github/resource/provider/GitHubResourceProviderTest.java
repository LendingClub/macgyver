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
package io.macgyver.github.resource.provider;

import io.macgyver.core.resource.Resource;
import io.macgyver.plugin.github.resource.provider.GitHubResourceProvider;
import io.macgyver.test.MacGyverIntegrationTest;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

public class GitHubResourceProviderTest extends MacGyverIntegrationTest{



	@Test
	@Ignore
	public void testX() throws IOException {
		
		GitHub gh = GitHub.connectAnonymously();
		
		GHRepository repo = gh.getRepository("if6was9/macgyver-resource-test");
		
		
	//	GitHub gh = GitHub.connectToEnterprise(getPrivateProperty("github.api.url"), getPrivateProperty("github.api.oauthToken"));
		
	
		
		
		GitHubResourceProvider p = new GitHubResourceProvider(gh);
		p.setRepoName("if6was9/macgyver-resource-test");
		p.setRef("refs/heads/master");
		
		for (Resource r:  p.findResources()) {
			logger.debug("{}",r.getContentAsString());
			logger.debug("{}",r);
		}
		
	}
}
