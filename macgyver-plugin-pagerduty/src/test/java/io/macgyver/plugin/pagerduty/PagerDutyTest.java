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
package io.macgyver.plugin.pagerduty;

import java.io.IOException;

import io.macgyver.core.MacGyverException;
import io.macgyver.core.service.ServiceRegistry;
import io.macgyver.plugin.pagerduty.PagerDutyClientImpl;
import io.macgyver.test.MacGyverIntegrationTest;

import org.aspectj.lang.annotation.Before;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;

public class PagerDutyTest extends MacGyverIntegrationTest {

	@Autowired
	ServiceRegistry registry;
	
	
	
	
	@Test(expected=PagerDutyInvocationException.class)
	public void testInvalidKey() {


		PagerDutyClientImpl c = new PagerDutyClientImpl();
		c.setServiceKey("aaaaaa");
		ObjectNode n = c.createIncident(null, "test summary", null, null,null);
	
		Assert.fail();
	}

	
	@Test(expected=MacGyverException.class)
	public void testMissingServiceKey() {


		PagerDutyClientImpl c = new PagerDutyClientImpl();

		c.createIncident(null, "test summary", null, null,null);
	
	}
	
	
	@Test
	public void testRequestFormat() {
		PagerDutyClientImpl c = new PagerDutyClientImpl();
		c.setServiceKey("aaaaaa");
		
		
		ObjectNode n = c.formatRequest("trigger", "ikey", "desc", "client", "http://example.com", null);
		
		
		Assert.assertEquals("trigger",n.path("event_type").asText());
		Assert.assertEquals("ikey",n.path("incident_key").asText());
		Assert.assertEquals("desc",n.path("description").asText());
		Assert.assertEquals("client",n.path("client").asText());
		Assert.assertEquals("http://example.com",n.path("client_url").asText());
	}
}
