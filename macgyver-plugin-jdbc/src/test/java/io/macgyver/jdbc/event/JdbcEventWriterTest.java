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
package io.macgyver.jdbc.event;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.davidmoten.rx.jdbc.Database;

import io.macgyver.core.event.EventLogger;
import io.macgyver.core.event.EventSystem;
import io.macgyver.core.event.MacGyverEventPublisher;
import io.macgyver.core.event.MacGyverMessage;
import io.macgyver.core.util.JsonNodes;
import io.macgyver.jdbc.JacksonRxJdbcResultSetMapper;
import io.macgyver.test.MacGyverIntegrationTest;

public class JdbcEventWriterTest extends MacGyverIntegrationTest {

	Logger logger = LoggerFactory.getLogger(JdbcEventWriterTest.class);
	static Database db;

	@Autowired
	MacGyverEventPublisher publisher;

	@Autowired
	JdbcEventWriter eventWriter;

	@Autowired
	EventLogger eventLogger;
	
	@Autowired
	EventSystem eventSystem;
	
	@Before
	public void setupTempDB() {

		if (db == null) {
			db = Database.builder().url("jdbc:h2:mem:event_test").pool(10,10).build();

			
				db.update(JdbcEventWriter.GENERIC_DDL).execute();
			
		}

		eventWriter.withDatabase(db);

	
	}

	@Test
	public void test() throws Exception {
	
		int count = 100;
	
		
		String uuid = UUID.randomUUID().toString(); 
		for (int i = 0; i < count; i++) {
			MacGyverMessage m = new MacGyverMessage();
			m.withAttribute("test", "hello " + uuid+" "+i);

			eventSystem.post(m);
	
		}

	
		long t0= System.currentTimeMillis();
		boolean ok = false;
		while ((!ok) && System.currentTimeMillis()-t0<60000) {
			int c = db.select("select * from event where JSON_DATA like '%"+uuid+"%'").get(new JacksonRxJdbcResultSetMapper()).toList().toBlocking().first().size();
			if (c==count) {
				ok=true;
			}
			logger.info("count: "+c);
			Thread.sleep(10);
		}
		
		Assertions.assertThat(ok).isTrue();

	}

	public static class TestMessageType extends MacGyverMessage {
		
	}
	@Test
	public void testX() throws InterruptedException, IOException, JsonProcessingException{
		
		CountDownLatch latch = new CountDownLatch(1);
		eventSystem.newObservable(MacGyverMessage.class).subscribe(x-> {
			
			if (x.getPayload().path("hello").asText().equals("world")) {
				latch.countDown();
			}
			
		});
		
	
		
		
		//MacGyverMessage m = eventLogger.event().withMessage("hello");
		MacGyverMessage m = publisher.createMessage().withMessageType(TestMessageType.class).withAttribute("hello", "world").publish();

		Assertions.assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();
		//Thread.sleep(5000);
		JsonNode envelope = m.getEnvelope();
	
		Assertions.assertThat(envelope.has("eventId")).isTrue();
		Assertions.assertThat(envelope.get("eventTs").asLong()).isCloseTo(System.currentTimeMillis(), Offset.offset(30000L));
		Assertions.assertThat(envelope.path("data").path("hello").asText()).isEqualTo("world");
		Assertions.assertThat(envelope.get("data")).isSameAs(m.getPayload());
		Assertions.assertThat(m.getPayload().size()).isEqualTo(1);
		Assertions.assertThat(envelope.path("eventType").asText()).isEqualTo(TestMessageType.class.getName());
		

	
		JsonNode x = db.select("select * from event where EVENT_ID=?").parameter(envelope.get("eventId").asText()).get(new JacksonRxJdbcResultSetMapper()).toBlocking().first();
		Assertions.assertThat(x.path("EVENT_ID").asText()).isEqualTo(m.getEventId()).isEqualTo(envelope.get("eventId").asText());
		
		JsonNode jsonFromDb = JsonNodes.mapper.readTree(x.path("JSON_DATA").asText());
		logger.info("EVENT row: \n{}",JsonNodes.pretty(x));
		
		logger.info("JSON_DATA from db: \n{}",JsonNodes.pretty(jsonFromDb));
		
		Assertions.assertThat(jsonFromDb.path("data").path("hello").asText()).isEqualTo("world");
		Assertions.assertThat(jsonFromDb.path("eventId").asText()).isEqualTo(envelope.get("eventId").asText());
		Assertions.assertThat(jsonFromDb.path("eventTs").asLong()).isEqualTo(envelope.get("eventTs").asLong());

	}

}
