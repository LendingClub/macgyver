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
package io.macgyver.core.service.config;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.lendingclub.neorx.NeoRxClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public class Neo4jConfigLoader extends ConfigLoader {

	Logger logger = LoggerFactory.getLogger(Neo4jConfigLoader.class);
	
	@Autowired
	NeoRxClient neo4j;
	
	@Override
	public void applyConfig(Map<String, String> m) {
		Preconditions.checkNotNull(neo4j);
		try {
		neo4j.execCypher("match (c:ServiceConfig) return c").forEach(x->{
			String serviceName = x.path("serviceName").asText();
			if (Strings.isNullOrEmpty(serviceName)) {
				x.fields().forEachRemaining(entry->{
					m.put(serviceName+"."+entry.getKey(), entry.getValue().asText());
				});
			}
		});
		}
		catch (RuntimeException e) {
			if (isIntegrationTest()) {
				// allow this to fail for integration tests only.  If neo4j is detected to be unavailable at test time, int tests will simply be skipped.
				logger.warn("neo4j connectivity failed for Neo4jConfigLoader...continuing because we are in an integration test",e);
				return;
			}
			throw e;
		}

	}
	
	
	boolean isIntegrationTest() {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		new RuntimeException().printStackTrace(pw);
		pw.close();
		
		if (sw.toString().contains("org.junit")) {
			return true;
		}
		return false;
	}

}
