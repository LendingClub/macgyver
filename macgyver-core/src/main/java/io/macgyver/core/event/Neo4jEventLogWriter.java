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

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.lendingclub.reflex.consumer.Consumers;
import org.lendingclub.reflex.predicate.Predicates;
import org.lendingclub.reflex.queue.WorkQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.macgyver.neorx.rest.NeoRxClient;
import io.reactivex.Observer;
import io.reactivex.functions.Consumer;


public class Neo4jEventLogWriter implements InitializingBean {

	public static final String EVENT_LOG_LABEL = "EventLog";

	Logger logger = LoggerFactory.getLogger(Neo4jEventLogWriter.class);

	@Autowired
	NeoRxClient neo4j;

	@Autowired
	EventSystem eventSystem;

	DateTimeFormatter utcFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX").withZone(ZoneOffset.UTC);

	protected void applyTimestamp(Instant instant, ObjectNode n) {
		if (instant == null) {

			long ts = n.path("eventTs").asLong();
			if (ts <= 0) {
				instant = Instant.now();
			} else {
				instant = Instant.ofEpochMilli(ts);
			}

		}

		n.put("eventTs", instant.toEpochMilli());

		n.put("eventDate", utcFormatter.format(instant));
	}

	protected void checkLabel(String label) {

		Preconditions.checkArgument(!Strings.isNullOrEmpty(label), "label cannot be empty");
		Preconditions.checkArgument(Character.isUpperCase(label.charAt(0)),
				"first character of label must be upper case");
		char[] c = label.toCharArray();
		for (int i = 0; i < c.length; i++) {
			Preconditions.checkArgument(Character.isLetterOrDigit(c[i]), "label must be alpha-numeric");
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		
		
		Consumer<LogMessage> consumer = new Consumer<LogMessage>() {

			@Override
			public void accept(LogMessage logEvent) {

				logger.debug("writing log message: {}", logEvent);
				if (neo4j != null) {
					String labelClause = "";
					String label = logEvent.getLabel();
					if (!Strings.isNullOrEmpty(label)) {
						checkLabel(label);
						labelClause = ":" + label;
					}
					JsonNode n = logEvent.getPayload();

					try {
						if (n != null && n.isObject()) {
							ObjectNode props = (ObjectNode) n;
							props = props.deepCopy();

							applyTimestamp(logEvent.getTimestamp(), props);

							String cypher = "create (x:EventLog" + labelClause + ") set x={props}";

							neo4j.execCypher(cypher, "props", props);
						}
					} catch (RuntimeException e) {
						logger.warn("problem logging to EventLog: " + n, e);
					}

				}

			}
		};
		WorkQueue<LogMessage> workQueue = new WorkQueue<LogMessage>().withThreadName("Neo4jEventLogWriter-%d");
		workQueue.getObservable().subscribe(Consumers.safeConsumer(consumer));
		eventSystem.getObservable().filter(Predicates.type(LogMessage.class)).subscribe(Consumers.safeConsumer(workQueue));
		
	}

}
