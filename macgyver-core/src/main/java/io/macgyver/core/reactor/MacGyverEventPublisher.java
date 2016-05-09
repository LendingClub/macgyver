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

import java.net.InetAddress;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import reactor.bus.Event;
import reactor.bus.EventBus;

public class MacGyverEventPublisher {

	Logger logger = LoggerFactory.getLogger(MacGyverEventPublisher.class);

	@Autowired
	EventBus eventBus;

	static ObjectMapper mapper = new ObjectMapper();

	public class MessageBuilder {

		MacGyverMessage message = new MacGyverMessage();

		public MessageBuilder() {

		}

		public MessageBuilder withMessageType(Class<? extends MacGyverMessage> clazz) {
			try {
				message = clazz.newInstance();

				return this;
			} catch (InstantiationException | IllegalAccessException e) {
				throw new IllegalArgumentException(e);
			}
		}
		
		public MessageBuilder withMessageBody(JsonNode n) {
			message.withData(n);
			return this;
		}
		public MessageBuilder withMessage(MacGyverMessage m) {
			this.message = m;
			return this;
		}

		public MessageBuilder withAttribute(String key, String val) {
			this.message.withAttribute(key, val);
			return this;
		}
		public MessageBuilder withAttribute(String key, JsonNode n) {
			this.message.withAttribute(key,n);
			return this;
		}
		
		public <T extends MacGyverMessage> T publish() {
			
			MacGyverEventPublisher.this.publishObject(message);

			return (T) message;
		}
	}

	public MacGyverEventPublisher() {

	}

	public MacGyverEventPublisher(EventBus bus) {
		this.eventBus = bus;
	}

	
	protected void publishObject(Object object) {
		if (eventBus != null) {
			eventBus.notify(object, Event.wrap(object));
		} else {
			logger.warn("event not published because EventBus was not set");
		}
	}

	public EventBus getEventBus() {
		return eventBus;
	}
	public void publish(MacGyverMessage message) {
		publishObject(message);
	}
	
	public MessageBuilder createMessage(Class<? extends MacGyverMessage> clazz) {
		MessageBuilder mb = new MessageBuilder();
		mb = mb.withMessageType(clazz);
		return mb;
	}

	public MessageBuilder createMessage() {
		return new MessageBuilder();
	}

	private static String getDefaultSource() {

		try {
			return InetAddress.getLocalHost().getHostName();

		} catch (Exception e) {
		}

		try {
			return InetAddress.getLocalHost().getHostAddress();

		} catch (Exception e) {
		}

		return "127.0.0.1";
	}
}