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
package io.macgyver.plugin.elb.a10;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public class A10HAClientImplTest {

	@Rule
	public MockWebServer mock1 = new MockWebServer();
	@Rule
	public MockWebServer mock2 = new MockWebServer();
	
	@Test
	public void testActiveCheckInterval() throws IOException {

		Assertions.assertThat(A10HAClientImpl.DEFAULT_NODE_CHECK_SECS)
				.isEqualTo(60);

	}
	
	@Test
	public void testHASelection() {
		A10ClientImpl c1 = new A10ClientImpl(mock1.url("/services/rest/v2/")
				.toString(), "dummyuser", "dummypassword");
		
		A10ClientImpl c2 = new A10ClientImpl(mock2.url("/services/rest/v2/")
				.toString(), "dummyuser", "dummypassword");
		
		c1.setAuthToken("prevent_auth");
		c2.setAuthToken("prevent_auth");
		
		
		// Set up response so that #2 is active and #1 is standby
		mock1
		.enqueue(new MockResponse()
				.setBody("<response status=\"ok\">\n" + 
						"  <ha_group_status_list>\n" + 
						"    <ha_group_status>\n" + 
						"      <id>1</id>\n" + 
						"      <local_status>0</local_status>\n" + 
						"      <local_priority>100</local_priority>\n" + 
						"      <peer_status>0</peer_status>\n" + 
						"      <peer_priority>50</peer_priority>\n" + 
						"      <force_self_standby>0</force_self_standby>\n" + 
						"    </ha_group_status>\n" + 
						"  </ha_group_status_list>\n" + 
						"</response>"));
		mock2
		.enqueue(new MockResponse()
				.setBody("<response status=\"ok\">\n" + 
						"  <ha_group_status_list>\n" + 
						"    <ha_group_status>\n" + 
						"      <id>1</id>\n" + 
						"      <local_status>1</local_status>\n" + 
						"      <local_priority>100</local_priority>\n" + 
						"      <peer_status>0</peer_status>\n" + 
						"      <peer_priority>50</peer_priority>\n" + 
						"      <force_self_standby>0</force_self_standby>\n" + 
						"    </ha_group_status>\n" + 
						"  </ha_group_status_list>\n" + 
						"</response>"));
		
		// Now verify this on each client individually
		Assertions.assertThat(c1.isActive()).isFalse();
		Assertions.assertThat(c2.isActive()).isTrue();
		
		// Set up the same test #2 active and #1 standby
		mock1
		.enqueue(new MockResponse()
				.setBody("<response status=\"ok\">\n" + 
						"  <ha_group_status_list>\n" + 
						"    <ha_group_status>\n" + 
						"      <id>1</id>\n" + 
						"      <local_status>0</local_status>\n" + 
						"      <local_priority>100</local_priority>\n" + 
						"      <peer_status>0</peer_status>\n" + 
						"      <peer_priority>50</peer_priority>\n" + 
						"      <force_self_standby>0</force_self_standby>\n" + 
						"    </ha_group_status>\n" + 
						"  </ha_group_status_list>\n" + 
						"</response>"));
		mock2
		.enqueue(new MockResponse()
				.setBody("<response status=\"ok\">\n" + 
						"  <ha_group_status_list>\n" + 
						"    <ha_group_status>\n" + 
						"      <id>1</id>\n" + 
						"      <local_status>1</local_status>\n" + 
						"      <local_priority>100</local_priority>\n" + 
						"      <peer_status>0</peer_status>\n" + 
						"      <peer_priority>50</peer_priority>\n" + 
						"      <force_self_standby>0</force_self_standby>\n" + 
						"    </ha_group_status>\n" + 
						"  </ha_group_status_list>\n" + 
						"</response>"));

	
		// Now establish an HA Client with the two
		A10HAClientImpl haClient = new A10HAClientImpl(c1,c2);
		
		// Verify that the HAClient figures out which is active and which is standby
		Assertions.assertThat(haClient.getActiveClient()).isSameAs(c2);
		Assertions.assertThat(haClient.getStandbyClient()).isSameAs(c1);
		
	
		// simulate a failover
		mock1
		.enqueue(new MockResponse()
				.setBody("<response status=\"ok\">\n" + 
						"  <ha_group_status_list>\n" + 
						"    <ha_group_status>\n" + 
						"      <id>1</id>\n" + 
						"      <local_status>1</local_status>\n" + 
						"      <local_priority>100</local_priority>\n" + 
						"      <peer_status>0</peer_status>\n" + 
						"      <peer_priority>50</peer_priority>\n" + 
						"      <force_self_standby>0</force_self_standby>\n" + 
						"    </ha_group_status>\n" + 
						"  </ha_group_status_list>\n" + 
						"</response>"));
		mock2
		.enqueue(new MockResponse()
				.setBody("<response status=\"ok\">\n" + 
						"  <ha_group_status_list>\n" + 
						"    <ha_group_status>\n" + 
						"      <id>1</id>\n" + 
						"      <local_status>0</local_status>\n" + 
						"      <local_priority>100</local_priority>\n" + 
						"      <peer_status>0</peer_status>\n" + 
						"      <peer_priority>50</peer_priority>\n" + 
						"      <force_self_standby>0</force_self_standby>\n" + 
						"    </ha_group_status>\n" + 
						"  </ha_group_status_list>\n" + 
						"</response>"));
		
		// need to reset the status or else the HAClient will cache the active client
		haClient.resetClientHAStatus(); // "forget" the active A10
		
		Assertions.assertThat(haClient.getActiveClient()).isSameAs(c1);
		Assertions.assertThat(haClient.getStandbyClient()).isSameAs(c2);
		

	}

}
