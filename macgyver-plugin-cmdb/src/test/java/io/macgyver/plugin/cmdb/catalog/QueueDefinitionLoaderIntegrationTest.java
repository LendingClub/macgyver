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
package io.macgyver.plugin.cmdb.catalog;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.Assume;
import org.junit.Test;
import org.lendingclub.neorx.NeoRxClient;

import com.fasterxml.jackson.databind.JsonNode;

import io.macgyver.plugin.git.GitResourceProvider;
import io.macgyver.test.MacGyverIntegrationTest;

public class QueueDefinitionLoaderIntegrationTest extends MacGyverIntegrationTest {

	@Inject
	NeoRxClient neo4j;

	@Inject
	org.springframework.context.ApplicationContext applicationContext;
	
	@Test
	public void testIt() {
		Assume.assumeFalse(isRunningInCircleCI());
		neo4j.execCypher("match (a:QueueDefinition) where a.id=~'junit-test.*' delete a");
		
		GitResourceProvider r = new GitResourceProvider("https://github.com/if6was9/macgyver-resource-test.git");

		
		QueueDefinitionLoader l = new QueueDefinitionLoader().withResourceProvider(r).withNeoRxClient(neo4j);
		
		l.importAll();

	
		JsonNode n = neo4j.execCypher("match (a:QueueDefinition) where a.id='junit-test-queue-1' return a").blockingFirst();
		
		Assertions.assertThat(n.path("foo").asText()).isEqualTo("bar");
		
		
		Assertions.assertThat(neo4j.execCypher("match (a:QueueDefinition) where a.id='junit-test-parse-error' return a").blockingFirst().path("error").asText()).contains("ParseException");
	}
	
	


	

}
