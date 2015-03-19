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

import io.macgyver.core.rest.OkRest;
import io.macgyver.core.service.ServiceRegistry;
import io.macgyver.core.test.StandaloneServiceBuilder;
import io.macgyver.test.MacGyverIntegrationTest;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;

public class GitHubTest extends MacGyverIntegrationTest {

	@Autowired
	ServiceRegistry reg;

	@Autowired
	GitHubServiceFactory githubServiceFactory;
	
	
	@Test
	public void testX() throws IOException {
		GitHub gh = (GitHub) reg.get("testGitHub");

		Assert.assertNotNull(gh);
		
		Assert.assertNotNull(githubServiceFactory);
	}
	
	
	@Test
	public void testIt() throws IOException {
		StandaloneServiceBuilder sb = StandaloneServiceBuilder.forServiceFactory(GitHubServiceFactory.class);
		
		GitHub gh = (GitHub) sb.build();
		
		Assertions.assertThat(gh).isNotNull();
		
		
		OkRest x = (OkRest) sb.buildCollaborator("Api");
		
		Assertions.assertThat(x.getUrl()).isEqualTo("https://api.github.com");
		
		
	}


}
