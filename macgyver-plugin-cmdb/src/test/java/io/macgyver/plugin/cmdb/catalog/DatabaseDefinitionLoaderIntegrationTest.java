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
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import io.macgyver.neorx.rest.NeoRxClient;
import io.macgyver.plugin.cmdb.catalog.AppDefinitionLoader;
import io.macgyver.plugin.git.GitResourceProvider;
import io.macgyver.test.MacGyverIntegrationTest;

public class DatabaseDefinitionLoaderIntegrationTest extends MacGyverIntegrationTest {

	@Inject
	NeoRxClient neo4j;

	@Inject
	org.springframework.context.ApplicationContext applicationContext;
	
	@Test
	public void testIt() {

		neo4j.execCypher("match (a:DatbaseDefinition) where a.id=~'junit-test.*' delete a");
		
		GitResourceProvider r = new GitResourceProvider("https://github.com/if6was9/macgyver-resource-test.git");

		
		DatabaseDefinitionLoader l = new DatabaseDefinitionLoader().withResourceProvider(r).withNeoRxClient(neo4j);
		
		l.importAll();

	
		
		JsonNode n = neo4j.execCypher("match (a:DatabaseDefinition) where a.id='junit-test-db' return a").toBlocking().first();
		
		Assertions.assertThat(n.path("type").asText()).isEqualTo("mysql");
	}


	

}
