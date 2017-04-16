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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Test;
import org.lendingclub.neorx.NeoRxClient;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;

import io.macgyver.core.Kernel;
import io.macgyver.core.cluster.ClusterManager;
import io.macgyver.core.scheduler.TaskStateManager.TaskState;
import io.macgyver.test.MacGyverIntegrationTest;
import it.sauronsoftware.cron4j.TaskExecutor;

public class TaskStateManagerIntegrationTest extends MacGyverIntegrationTest {

	@Inject
	TaskStateManager tsm;

	@Inject
	NeoRxClient neo4j;

	ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testGetMacGyverTask() {
		Assertions.assertThat(tsm.getMacGyverTask(null)).isEmpty();

		TaskExecutor te = Mockito.mock(TaskExecutor.class);

		Assertions.assertThat(tsm.getMacGyverTask(te)).isEmpty();

		MacGyverTask t = new MacGyverTask(mapper.createObjectNode().put("id", "foo"));
		te = Mockito.mock(TaskExecutor.class);

		Mockito.when(te.getTask()).thenReturn(t);

		MacGyverTask actual = tsm.getMacGyverTask(te).get();
		Assertions.assertThat(actual).isSameAs(t);
		Assertions.assertThat(actual.getTaskId()).isEqualTo("foo");

	}

	@After
	public void cleanup() {
		if (isNeo4jAvailable()) {
			neo4j.execCypher("match (t:TaskState) where t.id=~'junit_.*' detach delete t");
		}
	}

	@Test
	public void testUpdateStateOnNonExistentEntity() {
		Lists.newArrayList(TaskState.values()).forEach(it -> {
			try {
				tsm.recordUserDefinedTaskEnd("bogus_" + UUID.randomUUID().toString(), it);
				Assertions.failBecauseExceptionWasNotThrown(IllegalStateException.class);
			} catch (IllegalStateException e) {
				Assertions.assertThat(e).isInstanceOf(IllegalStateException.class);
			}
		});

	}

	@Test
	public void testIt() {

		String id = "junit_" + UUID.randomUUID().toString();
		tsm.recordUserDefinedTaskStart(id);

		List<JsonNode> list = neo4j.execCypher("match (t:TaskState {id:{id}}) return t", "id", id).toList().blockingGet();

		Assertions.assertThat(list.size()).isEqualTo(1);

		ObjectNode n = (ObjectNode) list.get(0);

		Assertions.assertThat(n.path("state").asText()).isEqualTo(TaskState.STARTED.toString());
		Assertions.assertThat(n.path("type").asText()).isEqualTo("user");
		Assertions.assertThat(n.has("processUuid")).isTrue();
		Assertions.assertThat(n.has("startDate")).isTrue();
		Assertions.assertThat(n.has("startTs")).isTrue();

		// now changes state

		tsm.recordUserDefinedTaskEnd(id, TaskState.COMPLETED);

		list = neo4j.execCypher("match (t:TaskState {id:{id}}) return t", "id", id).toList().blockingGet();

		Assertions.assertThat(list.size()).isEqualTo(1);

		n = (ObjectNode) list.get(0);

		Assertions.assertThat(n.path("state").asText()).isEqualTo(TaskState.COMPLETED.toString());
		Assertions.assertThat(n.path("type").asText()).isEqualTo("user");
		Assertions.assertThat(n.has("processUuid")).isTrue();
		Assertions.assertThat(n.has("startDate")).isTrue();
		Assertions.assertThat(n.has("startTs")).isTrue();
		Assertions.assertThat(n.has("endTs")).isTrue();

		Assertions.assertThat(n.has("endDate")).isTrue();

		// has now reached terminal state...attempt to set it to any other state
		// should fail

		Lists.newArrayList(TaskState.values()).forEach(it -> {
			try {
				tsm.recordUserDefinedTaskEnd(id, TaskState.CANCELLED);
				Assertions.failBecauseExceptionWasNotThrown(IllegalStateException.class);
			} catch (IllegalStateException e) {
				Assertions.assertThat(e).isExactlyInstanceOf(IllegalStateException.class);
			}
		});

		list = neo4j.execCypher("match (t:TaskState {id:{id}}) return t", "id", id).toList().blockingGet();

		Assertions.assertThat(list.size()).isEqualTo(1);

		n = (ObjectNode) list.get(0);

		Assertions.assertThat(n.path("state").asText()).isEqualTo(TaskState.COMPLETED.toString());
		Assertions.assertThat(n.path("type").asText()).isEqualTo("user");
		Assertions.assertThat(n.has("processUuid")).isTrue();
		Assertions.assertThat(n.has("startDate")).isTrue();
		Assertions.assertThat(n.has("startTs")).isTrue();
		Assertions.assertThat(n.has("endTs")).isTrue();

		Assertions.assertThat(n.has("endDate")).isTrue();
	}

	@Test
	public void testProcessId() {
		Assertions.assertThat(tsm.getProcessUuid())
				.isEqualTo(Kernel.getApplicationContext().getBean(ClusterManager.class).getLocalProcessId());
	}

	@Test
	public void testConcurrentCheck() {
		String id = "junit_" + UUID.randomUUID().toString();
		String teGuid = "junit_guid_" + UUID.randomUUID().toString();
		TaskExecutor te = Mockito.mock(TaskExecutor.class);
		Mockito.when(te.getGuid()).thenReturn(teGuid);
		MacGyverTask t = new MacGyverTask(mapper.createObjectNode().put("id", id));
		Mockito.when(te.getTask()).thenReturn(t);

		Assertions.assertThat(t.getTaskId()).isEqualTo(id);
		Assertions.assertThat(tsm.isAnotherTaskInstanceRunning(te)).isFalse();

		neo4j.execCypher(
				"merge (t:TaskState {id:{id},state:'COMPLETED'}) set t.type='cron4j', t.taskId={taskId} return t", "id",
				UUID.randomUUID().toString(), "taskId", id);

		Assertions.assertThat(tsm.isAnotherTaskInstanceRunning(te)).isFalse();

		neo4j.execCypher(
				"merge (t:TaskState {id:{id},state:'STARTED'}) set t.type='cron4j', t.taskId={taskId} return t", "id",
				UUID.randomUUID().toString(), "taskId", id);

		Assertions.assertThat(tsm.isAnotherTaskInstanceRunning(te)).isTrue();

	}

	@Test
	public void testOrphanTaskStateWithDeadProcess() {
		String taskId = "junit_" + UUID.randomUUID().toString();
		String teGuid = "junit_guid_" + UUID.randomUUID().toString();
		TaskExecutor te = Mockito.mock(TaskExecutor.class);
		Mockito.when(te.getGuid()).thenReturn(teGuid);
		MacGyverTask t = new MacGyverTask(mapper.createObjectNode().put("id", taskId));
		Mockito.when(te.getTask()).thenReturn(t);
		Mockito.when(te.isAlive()).thenReturn(true);
		Assertions.assertThat(t.getTaskId()).isEqualTo(taskId);
		Assertions.assertThat(tsm.isAnotherTaskInstanceRunning(te)).isFalse();

		tsm.recordTaskStart(te);

		// this will effectively orphan the TaskState...as if the process had
		// died and left a dangling node
		neo4j.execCypher("match (t:TaskState {id:{id}}) set t.processUuid='foobar'", "id", teGuid);

		tsm.marrkOrphanedTasksAsCancelled();

		neo4j.execCypher("match (t:TaskState {id:{id}}) return t", "id", teGuid).forEach(it -> {
			Assertions.assertThat(it.path("state").asText()).isEqualTo("CANCELLED");
		});
	}

	@Test
	public void testOrphanTaskStateWithLiveProcess() {
		String taskId = "junit_" + UUID.randomUUID().toString();
		String teGuid = "junit_guid_" + UUID.randomUUID().toString();
		TaskExecutor te = Mockito.mock(TaskExecutor.class);
		Mockito.when(te.getGuid()).thenReturn(teGuid);
		MacGyverTask t = new MacGyverTask(mapper.createObjectNode().put("id", taskId));
		Mockito.when(te.getTask()).thenReturn(t);
		Mockito.when(te.isAlive()).thenReturn(true);
		Assertions.assertThat(t.getTaskId()).isEqualTo(taskId);
		Assertions.assertThat(tsm.isAnotherTaskInstanceRunning(te)).isFalse();

		tsm.recordTaskStart(te);

		neo4j.execCypher("match (t:TaskState {id:{id}}) return t", "id", teGuid).forEach(it -> {
			Assertions.assertThat(it.path("state").asText()).isEqualTo("STARTED");
		});

		// now remove it from the local in-memory tracking set
		tsm.executingTaskSet.remove(teGuid);

		// this should now detect the errant record in neo4j and correct it
		tsm.marrkOrphanedTasksAsCancelled();

		neo4j.execCypher("match (t:TaskState {id:{id}}) return t", "id", teGuid).forEach(it -> {
			Assertions.assertThat(it.path("state").asText()).isEqualTo("CANCELLED");
		});
	}

	@Test
	public void testRecordTaskStart() {
		String taskId = "junit_" + UUID.randomUUID().toString();
		String teGuid = "junit_guid_" + UUID.randomUUID().toString();
		TaskExecutor te = Mockito.mock(TaskExecutor.class);
		Mockito.when(te.getGuid()).thenReturn(teGuid);
		MacGyverTask t = new MacGyverTask(mapper.createObjectNode().put("id", taskId));
		Mockito.when(te.getTask()).thenReturn(t);
		Mockito.when(te.isAlive()).thenReturn(true);
		Assertions.assertThat(t.getTaskId()).isEqualTo(taskId);
		Assertions.assertThat(tsm.isAnotherTaskInstanceRunning(te)).isFalse();

		tsm.recordTaskStart(te);

		neo4j.execCypher("match (t:TaskState {id:{id}}) return t", "id", teGuid).toList().blockingGet().forEach(it -> {
			
			Assertions.assertThat(it.path("id").asText()).isEqualTo(teGuid);
			Assertions.assertThat(it.path("taskId").asText()).isEqualTo(taskId);
			Assertions.assertThat(it.path("state").asText()).isEqualTo("STARTED");
			Assertions.assertThat(it.path("processUuid").asText()).isEqualTo(
					Kernel.getApplicationContext().getBean(ClusterManager.class).getLocalProcessId());
			Assertions.assertThat(it.path("type").asText()).isEqualTo("cron4j");
			Assertions.assertThat(it.path("hostname").asText().length()).isGreaterThan(0);
		});

		// Should be smart enough to determine that the task is ours
		Assertions.assertThat(tsm.isAnotherTaskInstanceRunning(te)).isFalse();

		// now create another

		TaskExecutor newTask = Mockito.mock(TaskExecutor.class);
		Mockito.when(newTask.getGuid()).thenReturn(UUID.randomUUID().toString());
		t = new MacGyverTask(mapper.createObjectNode().put("id", taskId));
		Mockito.when(newTask.getTask()).thenReturn(t);
		Mockito.when(newTask.isAlive()).thenReturn(true);
		Assertions.assertThat(tsm.isAnotherTaskInstanceRunning(newTask)).isTrue();

		// mark it as completed
		tsm.recordTaskEnd(te, TaskState.COMPLETED);

		// now the duplicate check should not be a problem
		Assertions.assertThat(tsm.isAnotherTaskInstanceRunning(newTask)).isFalse();

	}

	@Test
	public void testFormatInstant() {
		long epochMillis = 1457905611904L;
		String formattedDate = "2016-03-13T21:46:51.904Z";

		Instant x = Instant.ofEpochMilli(epochMillis);

		Assertions.assertThat(tsm.formatInstant(x)).isEqualTo(formattedDate);

	}
	

}
