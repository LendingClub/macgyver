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
package io.macgyver.core.scheduler;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.lendingclub.neorx.NeoRxClient;
import org.rapidoid.u.U;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.macgyver.core.cluster.ClusterManager;
import io.macgyver.core.scheduler.TaskStateManager.TaskState;
import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskExecutor;

/**
 * TaskStateManager keeps track of the state of running tasks. It is not
 * involved in the coordination or scheduling of tasks. Its sole pupose is to
 * keep track of state.
 * 
 * @author rschoening
 *
 */
public class TaskStateManager implements ApplicationListener<ApplicationReadyEvent>{

	public static enum TaskState {
		STARTED, COMPLETED, FAILED, CANCELLED;
	}

	ObjectMapper mapper = new ObjectMapper();

	@Autowired
	NeoRxClient neo4j;

	@Autowired
	ClusterManager clusterManager;

	Logger logger = LoggerFactory.getLogger(TaskStateManager.class);

	long purgeHorizon = 24;
	TimeUnit purgeHorizonUnit = TimeUnit.HOURS;

	Set<String> executingTaskSet = Sets.newConcurrentHashSet();

	

	public void recordUserDefinedTaskStart(String guid, String ...userData) {
		ObjectNode n = mapper.convertValue(U.map((Object[]) userData), ObjectNode.class);
		
		
	    recordUserDefinedTaskStart(guid, n);
	}
	public void recordUserDefinedTaskStart(String guid, JsonNode userData) {

		Instant now = Instant.now();

		if (userData == null) {
			userData = mapper.createObjectNode();
		}
		String cypher = "merge (t:TaskState {id:{guid}, type:'user'}) set t+={userData}, t.state={state},t.hostname={hostname},t.processUuid={processUuid}, t.startTs={nowTs}, t.startDate={date} return t";

		neo4j.execCypher(cypher, "guid", guid, "state", TaskState.STARTED.toString(), "hostname", getHostname(),
				"processUuid", getProcessUuid(), "nowTs", now.toEpochMilli(), "date", formatInstant(now), "userData",
				userData);
	}

	protected boolean isAnotherTaskInstanceRunning(TaskExecutor taskExecutor) {

		AtomicBoolean running = new AtomicBoolean(false);
		getMacGyverTask(taskExecutor).ifPresent(it -> {
			String taskId = it.config.path("id").asText();
			if (!Strings.isNullOrEmpty(taskId)) {
				List<JsonNode> runningTaskList = neo4j
						.execCypher("match (t:TaskState {taskId:{taskId}, state:'STARTED',type:'cron4j'}) where t.id<>{selfId} return t",
								"taskId", taskId, "selfId",taskExecutor.getGuid())
						.toList().blockingGet();

				if (!runningTaskList.isEmpty()) {
					// looks like we have a concurrent task execution;
					running.set(true);
				}
			}

		});
		return running.get();
	}

	protected void recordTaskStart(TaskExecutor executor) {

		String guid = executor.getGuid();

		Preconditions.checkState(executor.isAlive());

		if (isAnotherTaskInstanceRunning(executor)) {
			throw new ConcurrentExecutionNotAllowedException(getMacGyverTask(executor).get().getTaskId());
		}
		synchronized (this) {
			executingTaskSet.add(guid);
		}
		String host = getHostname();

		ObjectNode extraProps = mapper.createObjectNode();
		
		Optional<MacGyverTask> mt = getMacGyverTask(executor);
		if (mt.isPresent()) {
			ObjectNode tmp = (ObjectNode) mt.get().config.deepCopy();

			if (tmp.has("id")) {
				tmp.put("taskId", tmp.path("id").asText());
				tmp.remove("id");

			}
			tmp.remove("state");
			tmp.remove("hostname");
			tmp.remove("processUuid");
			tmp.remove("updateTs");
			tmp.put("description", tmp.path("taskId").asText());
			extraProps.setAll(tmp);
		}
		else {
			extraProps.put("description",executor.getTask().toString());
		}

		
		Instant now = Instant.now();

		String date = formatInstant(now);

		String cypher = "merge (t:TaskState {id:{guid}}) set t+={props},t.state={state},t.hostname={hostname},t.processUuid={processUuid},t.type='cron4j', t.startTs={ts}, t.startDate={date} return t";

		extraProps.remove("id"); // bad things if the id attibute is set
		neo4j.execCypher(cypher, "guid", guid, "processUuid", getProcessUuid(), "hostname", host, "ts",
				now.toEpochMilli(), "date", date, "state", TaskState.STARTED.toString(), "props", extraProps);

	}

	protected String getProcessUuid() {
		return clusterManager.getLocalProcessId();
	}

	protected String getHostname() {
		String host = "localhost";
		try {
			host = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			// stick with default
		}
		return host;
	}

	protected String formatInstant(Instant instant) {
		return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX").withZone(ZoneOffset.UTC).format(instant);
	}

	protected void recordTaskEnd(TaskExecutor executor, TaskState state) {

		Preconditions.checkArgument(executor != null);
		Preconditions.checkArgument(state != null);

		String guid = executor.getGuid();
		Instant now = Instant.now();

		String date = formatInstant(now);

		String cypher = "match (t:TaskState {id:{id}}) set t.endTs={ts},t.endDate={date} ,t.state={state} return t";

		neo4j.execCypher(cypher, "id", guid, "ts", now.toEpochMilli(), "date", date, "state", state.toString());
	}

	public void recordUserDefinedTaskEnd(String guid, TaskState state) {

		Preconditions.checkArgument(guid != null);
		Preconditions.checkArgument(state != null);
		Instant now = Instant.now();

		String date = formatInstant(now);

		Preconditions.checkState(state!=TaskState.STARTED,TaskState.STARTED+" is not a valid terminal state");
		String cypher = "match (t:TaskState {id:{id}, state:'STARTED'}) set t.endTs={ts},t.endDate={date} ,t.state={state} return t";

		int updateCount = neo4j.execCypher(cypher, "id", guid, "ts", now.toEpochMilli(), "date", date, "state", state.toString()).toList().blockingGet().size();
		if (updateCount==0) {
			throw new IllegalStateException("TaskState id="+guid+" could not be set to "+state);
		}

	}

	protected void purge() {
		try {
			purge(purgeHorizon, purgeHorizonUnit);
		} catch (RuntimeException e) {
			logger.warn("problem purging", e);
		}

	}

	protected void purge(long duration, TimeUnit unit) {
		long horizon = System.currentTimeMillis() - unit.toMillis(duration);

		logger.info("purging Task entries before {}", new Date(horizon));
		String cypher = "match (t:TaskState) where t.state<>'STARTED' and t.startTs<{horizon} delete t";

		neo4j.execCypher(cypher, "horizon", horizon);
	}



	protected void marrkOrphanedTasksAsCancelled() {

		// Now look for TaskState nodes in STARTED state that have a processUuid. This will tend to happen if
		List<String> processUuidList = Lists.newArrayList();

		Instant now = Instant.now();

		
		processUuidList.addAll(clusterManager.getProcessIdList());
	

		String cypher = "match (t:TaskState {state:'STARTED'}) where not exists(t.endTs) and not t.processUuid in {list} set t.endTs={endTs}, t.endDate={endDate}, t.state={state} return t";
		neo4j.execCypher(cypher, "list", processUuidList, "endTs", now.toEpochMilli(), "endDate", formatInstant(now),
				"state", TaskState.CANCELLED.toString());

		// ******************
		
		// Look at all the TaskState nodes in Neo4j for the current
		// process and identify any instances in STARTED state that are not
		// actually running locally. This is probably a bug.
		neo4j.execCypher("match (t:TaskState {processUuid:{processUuid}, state:'STARTED', type:'cron4j'})  return t",
				"processUuid", getProcessUuid()).forEach(it -> {
					String id = it.path("id").asText();
					if (!executingTaskSet.contains(id)) {

						logger.warn("neo4j TaskState does not match in-memory state: {}", it);
						neo4j.execCypher("match (t:TaskState {id:{id}}) set t.state={state}, t.endTs=timestamp()", "id",
								id, "state", TaskState.CANCELLED.toString());

					}
				});
	}

	

	public class OrphanedTaskCleanup implements Runnable {

		public static final String CRON = "* * * * *";

		@Override
		public void run() {
			try {
				marrkOrphanedTasksAsCancelled();
			} catch (Exception e) {
				logger.warn("problem marking terminated tasks", e);
			}

		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(OrphanedTaskCleanup.class).toString();
		}
	}

	public class AgingTaskCleanup implements Runnable {

		public static final String CRON = "*/5 * * * *";

		@Override
		public void run() {
			try {
				purge();
			} catch (Exception e) {
				logger.warn("problem purging old tasks", e);
			}
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(AgingTaskCleanup.class).toString();
		}
	}

	public static Optional<MacGyverTask> getMacGyverTask(TaskExecutor te) {
		if (te == null) {
			return Optional.empty();
		}

		Task task = te.getTask();
		if (task == null) {
			return Optional.empty();
		}

		if (task instanceof MacGyverTask) {
			return Optional.of((MacGyverTask) task);
		}
		return Optional.empty();
	}
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		
		try {
			neo4j.execCypher("create index on :TaskState(id)");
		}
		catch (RuntimeException e) {
			logger.warn("problem creating index on TaskState(id)");
		}
		
	}

}
