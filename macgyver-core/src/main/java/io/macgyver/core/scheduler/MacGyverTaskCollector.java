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
package io.macgyver.core.scheduler;

import java.util.List;

import org.lendingclub.neorx.NeoRxClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.beust.jcommander.internal.Lists;
import com.fasterxml.jackson.databind.JsonNode;

import io.macgyver.core.Kernel;
import io.macgyver.core.cluster.ClusterManager;
import it.sauronsoftware.cron4j.SchedulingPattern;
import it.sauronsoftware.cron4j.TaskCollector;
import it.sauronsoftware.cron4j.TaskTable;

public class MacGyverTaskCollector implements TaskCollector {

	Logger logger = LoggerFactory.getLogger(MacGyverTaskCollector.class);

	@Autowired
	NeoRxClient client;

	@Autowired
	ClusterManager clusterManager;

	public MacGyverTaskCollector() {

	}

	public List<JsonNode> fetchSchedule() {
		List<JsonNode> list = Lists.newArrayList();
		if (clusterManager.isPrimary()) {
			list.addAll(client
					.execCypherAsList("match (s:ScheduledTask) return s"));
			list.forEach(it -> {
				if (logger.isDebugEnabled()) {
					logger.debug("task: {}", it);
				}
			});
		}
		else {
			logger.info("scheduled tasks will not fire on this node because it is a secondary node in the cluster");
		}
		return list;
	}

	String enhanceCronExpression(String input) {
		input = input.trim();
		String output = input;

		return output;
	}

	protected TaskTable toTaskTable(List<JsonNode> list) {
		TaskTable tt = new TaskTable();

		ScheduledTaskManager stm = Kernel.getApplicationContext().getBean(ScheduledTaskManager.class);

		for (JsonNode n : list) {
			try {

				String cron = n.path("cron").asText();
				boolean enabled = stm.isEnabled(n);

				if (enabled
						&& !com.google.common.base.Strings.isNullOrEmpty(cron)) {
					MacGyverTask t = new MacGyverTask(n);
					tt.add(new SchedulingPattern(enhanceCronExpression(cron)),
							t);
				}
			} catch (RuntimeException e) {
				logger.warn("coulld not schedule task: " + n, e);
			}

		}

		return tt;
	}

	@Override
	public TaskTable getTasks() {

		return toTaskTable(fetchSchedule());
	}

}
