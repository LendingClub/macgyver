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

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.crsh.console.jline.internal.Preconditions;
import org.lendingclub.neorx.NeoRxClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.macgyver.core.Kernel;
import io.macgyver.core.scheduler.TaskStateManager.AgingTaskCleanup;
import io.macgyver.core.scheduler.TaskStateManager.OrphanedTaskCleanup;
import it.sauronsoftware.cron4j.Scheduler;

public class LocalScheduler implements Runnable, DirectScriptExecutor {

	static Logger logger = LoggerFactory.getLogger(LocalScheduler.class);

	ScheduledFuture<?> scheduledFuture;

	@Autowired
	Scheduler scheduler;

	@Autowired
	MacGyverTaskCollector taskCollector;

	@Autowired
	NeoRxClient neo4j;

	@Autowired
	TaskStateManager taskStateManager;

	synchronized NeoRxClient getNeoRxClient() {
		if (neo4j == null) {
			Kernel.getInstance();
			neo4j = Kernel.getApplicationContext().getBean(NeoRxClient.class);
		}
		return neo4j;
	}

	public boolean isMaster() {
		return true;
	}

	@PostConstruct
	public void startup() throws Exception {
		logger.info("starting scheduler...");

		scheduledFuture = Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(this, 0, 10, TimeUnit.SECONDS);

		Preconditions.checkNotNull(taskCollector);
		scheduler.addTaskCollector(taskCollector);
		scheduler.setDaemon(true);
		scheduler.addSchedulerListener(new MacGyverScheduleListener());

		scheduler.start();

		scheduler.schedule(OrphanedTaskCleanup.CRON, taskStateManager.new OrphanedTaskCleanup());
		scheduler.schedule(AgingTaskCleanup.CRON, taskStateManager.new AgingTaskCleanup());
	}

	@Override
	public void run() {
		try {
			Kernel.getApplicationContext().getBean(ScheduledTaskManager.class).scan();
		} catch (IOException e) {
			logger.warn("", e);
		}
	}

	/**
	 * 
	 * 
	 * @param scriptName
	 */
	public void executeScriptImmediately(String scriptName) {
		
		
		
		ObjectNode n = new ObjectMapper().createObjectNode();
		n.put("script", scriptName);
		try {
			String val = SecurityContextHolder.getContext().getAuthentication().getName();
			n.put("user", val);
		}
		catch (Exception e) {
			// debug level is fine...we tried
			logger.debug("could not determine current user",e);
		}
		MacGyverTask task = new MacGyverTask(n);
		scheduler.launch(task);
	}
}
