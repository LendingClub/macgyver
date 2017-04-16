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
package io.macgyver.core.reactor;

import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.uuid.Generators;

import io.macgyver.core.event.MacGyverEventPublisher;
import io.macgyver.core.event.MacGyverMessage;
import io.macgyver.core.util.JsonNodes;
import io.macgyver.core.event.MacGyverEventPublisher.MessageBuilder;

public class MacGyverMessageTest {

	ObjectMapper mapper = new ObjectMapper();

	
	public static class TestMessage extends MacGyverMessage {

	}

	@Test

	public void testTestMessage() {
		MacGyverEventPublisher p = new MacGyverEventPublisher();
		MacGyverMessage m = p.createMessage(TestMessage.class).publish();
		
		Assertions.assertThat(m).isInstanceOf(TestMessage.class);
		
		Assertions.assertThat(m.getEnvelope().isObject()).isTrue();
	}

	@Test
	public void testIt() {
		MacGyverMessage m = new MacGyverMessage();
		
		Assertions.assertThat(UUID.fromString(m.getEventId()).toString()).isEqualTo(m.getEventId());
		Assertions.assertThat(m.getEnvelope().get("eventTs").asLong()).isCloseTo(System.currentTimeMillis(), Offset.offset(5000L));
		Assertions.assertThat(m.getEnvelope().path("eventType").asText()).isEqualTo("io.macgyver.core.event.MacGyverMessage");
		Assertions.assertThat(m.getPayload().size()).isEqualTo(0);
		Assertions.assertThat(m.getPayload()).isSameAs(m.getEnvelope().get("data"));
	
	}

	
	@Test
	public void testIt2() {
		MacGyverMessage m = new MacGyverMessage().withAttribute("foo", "bar");
		
		Assertions.assertThat(UUID.fromString(m.getEventId()).toString()).isEqualTo(m.getEventId());
		Assertions.assertThat(m.getEnvelope().get("eventTs").asLong()).isCloseTo(System.currentTimeMillis(), Offset.offset(5000L));
		Assertions.assertThat(m.getEnvelope().path("eventType").asText()).isEqualTo("io.macgyver.core.event.MacGyverMessage");
		Assertions.assertThat(m.getPayload().path("foo").asText()).isEqualTo("bar");
		Assertions.assertThat(m.getPayload()).isSameAs(m.getEnvelope().get("data"));
	
	}
	
	
	@Test
	public void testIt3() {
		
		JsonNode payload = JsonNodes.mapper.createObjectNode().put("fizz", "buzz");
		
		MacGyverMessage m = new MacGyverMessage().withAttribute("foo", "bar").withData(payload);
		
		Assertions.assertThat(UUID.fromString(m.getEventId()).toString()).isEqualTo(m.getEventId());
		Assertions.assertThat(m.getEnvelope().get("eventTs").asLong()).isCloseTo(System.currentTimeMillis(), Offset.offset(5000L));
		Assertions.assertThat(m.getEnvelope().path("eventType").asText()).isEqualTo("io.macgyver.core.event.MacGyverMessage");
		Assertions.assertThat(m.getPayload().has("foo")).isFalse();
		Assertions.assertThat(m.getPayload().get("fizz").asText()).isEqualTo("buzz");
		Assertions.assertThat(m.getPayload()).isSameAs(m.getEnvelope().get("data"));
	
	}
	
}
