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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.lendingclub.neorx.NeoRxClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import io.macgyver.core.Kernel;
import io.macgyver.core.resource.Resource;
import io.macgyver.core.resource.ResourceMatcher;
import io.macgyver.core.script.ExtensionResourceProvider;

public class ScheduledTaskManager implements ApplicationListener<ApplicationReadyEvent> {

	public static final String SCHEDULE_TOKEN = "#@Schedule";
	public static final String SCHEDULED_BY_SCRIPT = "script";
	public static final String SCHEDULED_BY_MANUAL = "manual";
	public static final String SCHEDULED_BY = "scheduledBy";
	public static final String ENABLED = "enabled";

	Logger logger = LoggerFactory.getLogger(ScheduledTaskManager.class);

	@Autowired
	NeoRxClient neo4j;

	boolean schedulerEnabled = true;

	ObjectMapper mapper = new ObjectMapper();

	public void scheduleInline(String id, String cron, String script) {
		scheduleInline(id, cron, script, "groovy");
	}

	protected void throwIllegalStateOnEmptyList(String it, List<JsonNode> list) {
		if (list.isEmpty()) {
			throw new IllegalStateException("ScheduledTask id=" + it + " does not exist");
		}
	}

	public void scheduleInline(String id, String cron, String script, String language) {
		String cypher = "merge (t:ScheduledTask {id:{id}}) set t.scheduledBy='manual', t.cron={cron}, t.inlineScript={script}, t.enabled=true, t.inlineScriptLanguage={language} return t";
		throwIllegalStateOnEmptyList(id,
				neo4j.execCypher(cypher, "id", id, "cron", cron, SCHEDULED_BY_SCRIPT, script, "language", language)
						.toList().blockingGet());
	}

	public void updateSchedule(String id, String cron) {
		String cypher = "match (t:ScheduledTask {id:{id}}) set t.cron={cron} return t";
		throwIllegalStateOnEmptyList(id,
				neo4j.execCypher(cypher, "id", id, "cron", cron).toList().blockingGet());

	}

	public void disable(String id) {
		enable(id, false);
	}

	public void enable(String id) {
		enable(id, true);
	}

	public void enable(String id, boolean b) {
		String cypher = "match (t:ScheduledTask {id:{id}}) set t.enabled={enabled} return t";
		throwIllegalStateOnEmptyList(id, neo4j.execCypher(cypher, "id", id, ENABLED, b).toList().blockingGet());
	}

	public void scheduleManually(String id) {
		throwIllegalStateOnEmptyList(id,
				neo4j.execCypher("match (t:ScheduledTask {id:{id}}) set t.scheduledBy='manual' return t", "id", id)
						.toList().blockingGet());
	}

	public void scheduleByScript(String id) {
		throwIllegalStateOnEmptyList(id,
				neo4j.execCypher(
						"match (t:ScheduledTask {id:{id}}) where length(t.script)>0 set t.scheduledBy='script' return t",
						"id", id).toList().blockingGet());
	}

	public boolean isEnabled(JsonNode config) {
		return schedulerEnabled && config.path(ENABLED).asBoolean(true);
	}

	public void setSchedulerEnabled(boolean b) {
		logger.info("setting global scheduler status: {}", b);
		this.schedulerEnabled = b;
	}

	public boolean isSchedulerEnabled() {
		return this.schedulerEnabled;
	}

	protected Map<String, JsonNode> loadScheduledScriptTasks() {

		Map<String, JsonNode> m = Maps.newConcurrentMap();
		String cypher = "match (s:ScheduledTask) where length(s.script)>0 return s";
		neo4j.execCypher(cypher).forEach(it -> {
			String name = it.path(SCHEDULED_BY_SCRIPT).asText();
			if (!Strings.isNullOrEmpty(name)) {
				m.put(name, it);
			}
		});
		return m;

	}

	protected boolean isScripptScheduledByScript(JsonNode n) {
		return n != null && n.path(SCHEDULED_BY).asText().equals(SCHEDULED_BY_SCRIPT);
	}

	protected boolean isScriptManuallyScheduled(JsonNode n) {
		return n != null && n.path(SCHEDULED_BY).asText().equals(SCHEDULED_BY_MANUAL);
	}

	class ScriptResourceMatcher implements ResourceMatcher {

		@Override
		public boolean matches(Resource r) {
			try {
					if (r.getPath().startsWith("scripts/")) {
					return true;
				}
				return false;
			} catch (Exception e) {
				return false;
			}
		}

	}

	public void scan() throws IOException {

		Map<String, JsonNode> scriptMap = loadScheduledScriptTasks();

		Kernel.getInstance();
		ExtensionResourceProvider extensionLoader = Kernel.getApplicationContext()
				.getBean(ExtensionResourceProvider.class);
		long scanTime = System.currentTimeMillis();

		CrontabExpressionExtractor expressionExtractor = new CrontabExpressionExtractor();

		for (Resource r : extensionLoader.findResources(new ScriptResourceMatcher())) {
			if (logger.isDebugEnabled()) {
				logger.debug("evaluating {} to see if it can be scheduled", r);
			}
			String path = r.getPath();
			if (path != null && path.startsWith("scripts/scheduler/")) {

				ObjectNode descriptor = expressionExtractor.extractCronExpression(r).or(mapper.createObjectNode());

				boolean b = descriptor.path(ENABLED).asBoolean(true);

				if (isScriptManuallyScheduled(scriptMap.get(path))) {

					// if the ScheduledTask node's scheduledBy attribute is set
					// to SCHEDULED_BY_MANUAL in neo4j, do not update. This
					// allows scripts enabled/cron attributes to be
					// manually adjusted (outside of the script).

					if (logger.isDebugEnabled()) {
						logger.debug("manually scheduled script tassk will not be updated: {}", path);
					}
				} else {

					// Update the corresponding ScheduledTask node in neo4j. The
					// cron4j TaskCollector will read this.
					String cypher = "merge (s:ScheduledTask {id:{id}}) set s.script={id},s.scheduledBy='script', s.enabled={enabled}, s.cron={cron}, s.lastUpdateTs={ts} return s;";

					neo4j.execCypher(cypher, "id", path, ENABLED, b, "cron", descriptor.path("cron").asText(),
							"ts", scanTime);
				}

			}
		}

		// now remove all entries scheduled via script that were not just
		// updated

		if (logger.isDebugEnabled()) {
			logger.debug("removing old scheduled entries...");
		}
		String cypher = "match (s:ScheduledTask) where s.scheduledBy='script' and (s.lastUpdateTs is null or s.lastUpdateTs<{ts}) delete s";
		neo4j.execCypher(cypher, "ts", scanTime);

	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		try {
			// this is a one-time migration to use "id" as the identity
			// attribute
			neo4j.execCypher(
					"match (t:ScheduledTask) where exists(t.script) and (t.id<>t.script or not exists(t.id)) set t.id=t.script return t");
		} catch (RuntimeException e) {
			logger.warn("problem matching ScheduledTask nodes", e);
		}

		try {
			neo4j.execCypher("create index on :ScheduledTask(id)");
		} catch (RuntimeException e) {
			logger.warn("problem creating index on TaskState(id)");
		}

	}

}
