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

import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.google.common.hash.Hashing;

import io.macgyver.core.Kernel;
import io.macgyver.core.event.EventLogger;
import io.macgyver.core.event.Neo4jEventLogWriter;
import it.sauronsoftware.cron4j.SchedulerListener;
import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskExecutor;

public class MacGyverScheduleListener implements SchedulerListener {

	Logger logger = LoggerFactory.getLogger(MacGyverScheduleListener.class);

	public static final AtomicLong threadIdSequence = new AtomicLong(1);

	public static final String MDC_TASK = "task";

	long getElapsedTime(TaskExecutor te) {
		return System.currentTimeMillis() - te.getStartTime();
	}

	@Override
	public void taskFailed(TaskExecutor taskExecutor, Throwable exception) {

		if (taskExecutor.getTask() instanceof MacGyverTask) {
			logger.warn("taskFailed - " + taskExecutor.getTask() + " (" + getElapsedTime(taskExecutor) + " ms)", exception);
		}
		
		Kernel.getApplicationContext().getBean(TaskStateManager.class).recordTaskEnd(taskExecutor,
				TaskStateManager.TaskState.FAILED);
		MDC.remove(MDC_TASK);
	}

	@Override
	public void taskLaunching(TaskExecutor taskExecutor) {

		changeThreadName();

		Task t = taskExecutor.getTask();
		if (t instanceof MacGyverTask) {
			MacGyverTask mt = (MacGyverTask) t;
			MDC.put(MDC_TASK, mt.getTaskId());
		}

		Kernel.getApplicationContext().getBean(TaskStateManager.class).recordTaskStart(taskExecutor);

		if (taskExecutor.getTask() instanceof MacGyverTask) {
			logger.info("taskLaunching: {}", taskExecutor.getTask());
		}
	}

	@Override
	public void taskSucceeded(TaskExecutor taskExecutor) {
		
		if (taskExecutor.getTask() instanceof MacGyverTask) {
			logger.info("taskSucceeded: {}", taskExecutor.getTask() + " (" + getElapsedTime(taskExecutor) + " ms)");
		}
		
		Kernel.getApplicationContext().getBean(TaskStateManager.class).recordTaskEnd(taskExecutor,
				TaskStateManager.TaskState.COMPLETED);
		MDC.remove(MDC_TASK);
	}

	public void changeThreadName() {

		// the cron4j thread name is obnoxiously long
		// cron4j::scheduler[20db848d6c7825764442c08c000001536eaba9fd565cd406]::executor[20db848d6c78257671335d3a000001536eb21d417c3ceb53]

		try {
			Thread thread = Thread.currentThread();
			String threadName = thread.getName();

			if (threadName.contains("cron4j::scheduler")) {

				threadName = "cron4j-" + threadIdSequence.getAndIncrement();
				thread.setName(threadName);

			}
		} catch (Throwable e) {
			logger.warn("could not set thread name", e);
		}
	}
}
