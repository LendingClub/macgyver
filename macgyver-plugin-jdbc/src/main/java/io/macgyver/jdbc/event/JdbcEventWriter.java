package io.macgyver.jdbc.event;

import java.sql.Timestamp;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.lendingclub.reflex.consumer.Consumers;
import org.lendingclub.reflex.predicate.FlatMapFilters;
import org.lendingclub.reflex.queue.WorkQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;
import com.github.davidmoten.rx.jdbc.Database;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.macgyver.core.ServiceNotFoundException;
import io.macgyver.core.event.EventLogger;
import io.macgyver.core.event.EventSystem;
import io.macgyver.core.event.MacGyverEventPublisher;
import io.macgyver.core.event.MacGyverMessage;
import io.macgyver.core.service.ServiceRegistry;


public class JdbcEventWriter implements ApplicationListener<ApplicationReadyEvent> {

	@Autowired
	ServiceRegistry serviceRegistry;

	@Autowired
	MacGyverEventPublisher publisher;

	@Autowired
	EventLogger eventLogger;

	@Autowired 
	EventSystem eventSystem;
	
	TimeBasedGenerator uuidGenerator = Generators.timeBasedGenerator(EthernetAddress.fromInterface());

	AtomicReference<Database> database = new AtomicReference<Database>(null);

	Logger logger = LoggerFactory.getLogger(JdbcEventWriter.class);

	ObjectMapper mapper = new ObjectMapper();

	AtomicBoolean enabled = new AtomicBoolean(true);

	WorkQueue<MacGyverMessage> workQueue;
	
	class LocalConsumer implements io.reactivex.functions.Consumer<MacGyverMessage> {

		@Override
		public void accept(MacGyverMessage t) {
			write(t.getEnvelope());
		}

	}


	public Database getDatabase() {
		return database.get();
	}



	public JdbcEventWriter subscribe(EventSystem eventSystem) {
		this.eventSystem = eventSystem;
		this.workQueue = new WorkQueue<MacGyverMessage>().withCoreThreadPoolSize(2).withThreadName("JdbcEventWriter-%d");
		this.workQueue.getObservable().subscribe(Consumers.safeConsumer(new LocalConsumer()));
		
		this.eventSystem.getObservable().flatMap(FlatMapFilters.type(MacGyverMessage.class)).subscribe(Consumers.safeConsumer(workQueue));

		return this;
	}

	public JdbcEventWriter withDatabase(Database db) {
		database.set(db);
		return this;
	}

	public static final String GENERIC_DDL = "create table event (event_id varchar(38) not null, event_type varchar(150), json_data clob, event_ts timestamp)";
	public static final String MYSQL_DDL = "create table event (event_id varchar(38) not null, event_type varchar(150), json_data json, event_ts timestamp)";

	public void writeAsync(MacGyverMessage m) {
		Preconditions.checkState(eventSystem != null, "event bus not set");
		eventSystem.post(m);
	}

	public void write(MacGyverMessage m) {
		write(m.getEnvelope());
	}

	protected void write(JsonNode data) {
		try {
			if (!isEnabled()) {
				return;
			}
			if (data == null || !data.isObject()) {
				return;
			}
			String jsonString = mapper.writeValueAsString(data);

			if (Strings.isNullOrEmpty(jsonString)) {
				return;
			}

		
			String id = data.path("eventId").asText();
			if (Strings.isNullOrEmpty(id)) {
				
				id = uuidGenerator.generate().toString();
			}
			

			String eventType = data.path("eventType").asText();

			if (Strings.isNullOrEmpty(eventType)) {
				throw new IllegalStateException("eventType not set");
			}
			long ts = data.path("eventTs").longValue();
			if (ts <= 0) {
				ts = System.currentTimeMillis();
			}
			Timestamp eventTimestamp = new Timestamp(ts);

			int count = getDatabase()
					.update("insert into event(event_id, event_type, json_data,event_ts) values (?,?,?,?)")
					.parameter(id).parameter(eventType).parameter(jsonString).parameter(eventTimestamp).execute();
			logger.debug("inserted event id={} eventType={}", id, eventType);
		} catch (JsonProcessingException | RuntimeException e) {
			logger.warn("could not log event", e);
		}
	}



	public boolean isEnabled() {
		return enabled.get() && database.get() != null;
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		try {
			logger.info("configuring JdbcEventWriter");
			Database db = serviceRegistry.get("jdbcEventLogRxJdbc", Database.class);
			this.database.set(db);

			logger.info("configured JdbcEventLogWriter with db: {}", db);

		} catch (ServiceNotFoundException e) {
			logger.warn("service id={} not defined....events will not be logged to RDBMS", e.getMessage());
		} catch (RuntimeException e) {
			logger.error("could not load service id=jdbcEventLog...events will not be logger to RDBMS", e);
		}
	}
}
