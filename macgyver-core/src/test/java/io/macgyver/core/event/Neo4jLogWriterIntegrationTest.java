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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.macgyver.core.event;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.junit.After;
import org.junit.Test;
import org.lendingclub.neorx.NeoRxClient;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.macgyver.test.MacGyverIntegrationTest;

public class Neo4jLogWriterIntegrationTest extends MacGyverIntegrationTest {

	ObjectMapper m = new ObjectMapper();

	@Autowired
	EventLogger eventLogger;

	@Autowired
	NeoRxClient neo4j;

	@Autowired
	Neo4jEventLogWriter logWriter;
	
	@After
	public void cleanup() {
		try {
		neo4j.execCypher("match (x:EventLog) where exists(x.foo) delete x");
		}
		catch (Exception e) {
			logger.warn("could not clean up",e);
		}
	}
	@Test
	public void testLog() throws InterruptedException, IOException {

		String id = UUID.randomUUID().toString();
		((LogMessage) eventLogger.event().withAttribute("foo", id)).log();

		Thread.sleep(5000L);

		List<JsonNode> x = neo4j.execCypherAsList("match (x:EventLog) where x.foo={foo} return x", "foo", id);
	
		Assertions.assertThat(x.size()).isEqualTo(1);
		
		Assertions.assertThat(x.get(0).path("eventTs").asLong()).isCloseTo(System.currentTimeMillis(), Offset.offset(10000L));
		Assertions.assertThat(x.get(0).path("eventDate").asText()).startsWith("20").endsWith("Z");
		Assertions.assertThat(x.get(0).path("foo").asText()).isEqualTo(id);
		
		
	}
	@Test
	public void testLogWithTimestamp() throws InterruptedException, IOException {

		long ts = 1461756195921L;
		String tsd = "2016-04-27T11:23:15.921Z";
		
		String id = UUID.randomUUID().toString();
		((LogMessage)eventLogger.event().withAttribute("foo", id).withTimestamp(ts)).log();

		Thread.sleep(500L);

		List<JsonNode> x = neo4j.execCypherAsList("match (x:EventLog) where x.foo={foo} return x", "foo", id);

		
		Assertions.assertThat(x.size() == 1);
		
		Assertions.assertThat(x.get(0).path("eventTs").asLong()).isEqualTo(ts);
		Assertions.assertThat(x.get(0).path("eventDate").asText()).isEqualTo(tsd);
		Assertions.assertThat(x.get(0).path("foo").asText()).isEqualTo(id);
		
		
	}
	@Test
	public void testLogAlternateLabel() throws InterruptedException, IOException {

			
		String id = UUID.randomUUID().toString();
		((LogMessage)eventLogger.event().withAttribute("foo", id)).withLabel("TestEventLog").log();

		Thread.sleep(500L);

		List<JsonNode> x = neo4j.execCypherAsList("match (x:TestEventLog) where x.foo={foo} return x", "foo", id);

		
		Assertions.assertThat(x.size() == 1);
		
		Assertions.assertThat(x.get(0).path("eventTs").asLong()).isCloseTo(System.currentTimeMillis(), Offset.offset(2000L));

		Assertions.assertThat(x.get(0).path("foo").asText()).isEqualTo(id);
		
	}
	@Test
	public void testApplyTimetamp() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
		.withZone(ZoneOffset.UTC);
		
		ObjectNode n = m.createObjectNode();
		logWriter.applyTimestamp(null, n);
		Assertions.assertThat(n.path("eventTs").asLong()).isCloseTo(System.currentTimeMillis(), Offset.offset(1000L));
		Assertions.assertThat(n.path("eventDate").asText()).startsWith("20").endsWith("Z");
		
	
		
		n = m.createObjectNode().put("eventTs",1461754141054L);
		logWriter.applyTimestamp(null, n);
		Assertions.assertThat(n.path("eventTs").asLong()).isEqualTo(1461754141054L);
		Assertions.assertThat(n.path("eventDate").asText()).isEqualTo("2016-04-27T10:49:01.054Z");
		
		Instant now = Instant.now();
		
		n = m.createObjectNode().put("eventTs",1461754141054L);
		logWriter.applyTimestamp(now, n);
		Assertions.assertThat(n.path("eventTs").asLong()).isEqualTo(now.toEpochMilli());
		Assertions.assertThat(n.path("eventDate").asText()).isEqualTo(dtf.format(now));
		
		n = m.createObjectNode().put("eventTs","some non-numeric");
		logWriter.applyTimestamp(null, n);
		Assertions.assertThat(n.path("eventTs").asLong()).isCloseTo(Instant.now().toEpochMilli(), Offset.offset(1000L));
		Assertions.assertThat(n.path("eventDate").asText()).isEqualTo(dtf.format(Instant.ofEpochMilli(n.path("eventTs").asLong())));
	}

}
