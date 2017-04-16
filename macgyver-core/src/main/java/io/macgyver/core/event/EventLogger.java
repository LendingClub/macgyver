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

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public final class EventLogger {

	static Logger logger = LoggerFactory.getLogger(EventLogger.class);

	@Autowired
	MacGyverEventPublisher publisher;

	ObjectMapper mapper = new ObjectMapper();

	public Supplier<String> sourceHostSupplier = Suppliers.memoize(new SourceHostSupplier());


	public static class SourceHostSupplier implements Supplier<String> {
		@Override
		public String get() {
			try {
				String host = InetAddress.getLocalHost().getHostName();
				return host;
			} catch (UnknownHostException | RuntimeException e) {
				logger.warn("could not obtain local host name", e);
			}
			return "localhost";
		}
	}



	public LogMessage event() {
		return new LogMessage(this);
	}

	protected final void logEvent(LogMessage event) {

		try {

			// now log to the distributed system
			Preconditions.checkNotNull(publisher);
			Preconditions.checkNotNull(event,"event cannot be null");
			Preconditions.checkState(event.sent == false, "event already published");
			JsonNode data = event.getPayload();
			logger.info("logEvent data: {}",data);

			publisher.createMessage().withMessage(event).publish();
			event.sent = true;
		} catch (RuntimeException e) {
			logger.warn("could not publish log event", e);
		}
	}

}
