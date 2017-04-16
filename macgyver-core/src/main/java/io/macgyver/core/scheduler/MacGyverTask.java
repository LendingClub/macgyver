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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;

import io.macgyver.core.Bootstrap;
import io.macgyver.core.MacGyverException;
import io.macgyver.core.script.ScriptExecutor;
import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskExecutionContext;

public class MacGyverTask extends Task {

	Logger logger = LoggerFactory.getLogger(MacGyverTask.class);
	JsonNode config;

	public MacGyverTask(JsonNode n) {
		Preconditions.checkNotNull(n);
		this.config = n;
	}

	@Override
	public void execute(TaskExecutionContext context) throws RuntimeException {

		
	
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("execute {} context={}", this, context);
			}

			if (config.has("script")) {
				ScriptExecutor se = new ScriptExecutor();

				Map<String, Object> args = createArgsFromConfig();

				se.run(config.path("script").asText(), args, true);
			} else if (config.has("inlineScript")) {

				String language = config.path("inlineScriptLanguage").asText("groovy");

				String n = UUID.randomUUID().toString() + "." + language;

				File tempScriptsDir = new File(Bootstrap.getInstance().getScriptsDir().getAbsolutePath(), "temp");
				tempScriptsDir.mkdirs();

				File scriptFile = new File(tempScriptsDir, n);

				try {
					String scriptBody = config.path("inlineScript").asText();
					if (logger.isInfoEnabled()) {
						logger.info("executing inline script: {}", scriptBody);
					}
					Files.write(scriptBody, scriptFile, Charsets.UTF_8);

					ScriptExecutor se = new ScriptExecutor();

					Map<String, Object> args = createArgsFromConfig();

					se.run("temp/" + n, args, true);
				} finally {
					if (scriptFile.exists()) {
						try {
							scriptFile.delete();
						} catch (Exception e) {
							logger.warn("could not delete file: {}", scriptFile);
						}
					}
				}

			}
		} catch (IOException e) {
			throw new MacGyverException(e);
		}
	}

	public String toString() {
		ToStringHelper helper = MoreObjects.toStringHelper(this);
		if (config!=null) {
			config.fields().forEachRemaining(x->{
				helper.add(x.getKey(), x.getValue());
			});
		}
		return helper.toString();	
	}

	public String getTaskId() {
		return config.path("id").asText(null);
	}
	Map<String, Object> createArgsFromConfig() {
		Map<String, Object> args = com.google.common.collect.Maps.newHashMap();
		JsonNode params = config.path("parameters");
		Iterator<String> t = params.fieldNames();
		while (t.hasNext()) {
			String param = t.next();

			JsonNode val = params.get(param);
			if (val.isValueNode()) {
				args.put(param, val.asText());
			}
		}

		return args;
	}
	
	
}
