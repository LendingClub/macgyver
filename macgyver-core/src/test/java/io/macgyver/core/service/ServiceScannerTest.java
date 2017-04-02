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
package io.macgyver.core.service;

import org.junit.Test;
import org.lendingclub.neorx.NeoRxClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.macgyver.core.graph.NodeInfo;
import io.macgyver.test.MacGyverIntegrationTest;
import rx.functions.Action1;

public class ServiceScannerTest extends MacGyverIntegrationTest {

	Logger logger = LoggerFactory.getLogger(ServiceScannerTest.class);
	
	@Autowired
	NeoRxClient neorx;
	
	public static class TestScanner extends ServiceScanner<String> {

		public TestScanner(NeoRxClient neo4j, String service) {
			super(neo4j, service);
			
		}

		@Override
		public void scan() {
			
			
		}
		
	}
	
	
	@Test
	public void testIt() {
		
		
		

		TestScanner ts = new TestScanner(neorx, "dummy");
		
		Action1<NodeInfo> action = new Action1<NodeInfo>() {

			@Override
			public void call(NodeInfo t1) {
				logger.debug("");
				
			}};
		ts.addDecorationAction(action);
		
		ObjectNode n = new ObjectMapper().createObjectNode().put("foo", "bar");
		
		ts.decorate(0,n);
		

		
	}
}
