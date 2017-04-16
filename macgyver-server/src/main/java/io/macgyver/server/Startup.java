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
package io.macgyver.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.common.collect.Lists;

import groovy.lang.Writable;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;

@Component
public class Startup implements ApplicationListener<ApplicationReadyEvent> {

	@Autowired
	ApplicationContext applicationContext;

	Logger logger = LoggerFactory.getLogger(Startup.class);

	File tempDir;

	File targetDir = new File(".");
	File targetLibDir = new File(targetDir, "lib");

	JsonNode pluginDescriptor;

	List<String> dependencies = Lists.newArrayList();

	public Startup() {
		super();

		tempDir = Files.createTempDir();
		logger.info("temp dir: {}", tempDir);

		targetLibDir = new File(targetDir, "lib");
	}

	protected void readPlugins() throws IOException, JsonProcessingException {
		File pluginsFile = new File("./config/plugins.json");

		ObjectMapper mapper = new ObjectMapper();

		pluginDescriptor = mapper.readTree(pluginsFile);

		String version = pluginDescriptor.path("version").asText("1.1.1");

		pluginDescriptor.path("plugins").forEach(it -> {
			if (it.path("enabled").asBoolean(true)) {
				String group = it.path("group").asText("io.macgyver");
				String name = it.path("name").asText();
				String v = it.path("version").asText(version);

				String val = group + ":" + name + ":" + v;
				dependencies.add(val);
			}
		});

		if (!dependencies.stream().anyMatch(it -> it.startsWith("io.macgyver:macgyver-core:"))) {
			dependencies.add("io.macgyver:macgyver-core:" + version);
		}
	}

	protected void copyResource(String source, File target) throws IOException {
		target.getParentFile().mkdirs();
		logger.info("writing {} to {}", source, target);
		try (InputStream is = applicationContext.getResource(source).getInputStream()) {
			java.nio.file.Files.copy(is, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}

	}

	public void copyPluginsJson() throws IOException {
		File target = new File(targetDir, "config/plugins.json");
		if (!target.exists()) {
			copyResource("classpath:templates/plugins.json", target);
		}
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {

		try {
			copyPluginsJson();
			readPlugins();
			Resource resource = applicationContext.getResource("classpath:templates/build.gradle.template");

			File buildGradleFile = new File(tempDir, "build.gradle");
			SimpleTemplateEngine ste = new SimpleTemplateEngine();
			Template template = ste.createTemplate(new InputStreamReader(resource.getInputStream()));
			FileWriter fw = new FileWriter(buildGradleFile);

			Map<String, Object> x = Maps.newConcurrentMap();
			x.put("dependencies", dependencies);
			template.make(x).writeTo(fw);

			fw.close();

			logger.info("resource: {}", resource);

			ProjectConnection connect = GradleConnector.newConnector().forProjectDirectory(tempDir).connect();

			logger.info("{}", connect);
			BuildLauncher launcher = connect.newBuild();
			launcher.setStandardOutput(System.out);
			launcher.forTasks("fetch").run();

			targetLibDir.mkdirs();

			Arrays.asList(targetLibDir.listFiles()).forEach(it -> {
				it.delete();
			});

			Arrays.asList(new File(tempDir, "jars").listFiles()).forEach(it -> {
				try {
					File targetFile = new File(targetLibDir, it.getName());
					logger.info("using {}", targetFile.getName());
					Files.copy(it, targetFile);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});

			connect.close();

			File logbackFile = new File(targetDir, "config/logback.xml");

			if (!logbackFile.exists()) {
				logbackFile.getParentFile().mkdirs();
				try (FileOutputStream fos = new FileOutputStream(logbackFile)) {
					ByteStreams.copy(applicationContext.getResource("classpath:templates/logback.xml").getInputStream(),
							fos);
				}
			}

			File targetFile = new File(targetDir, "bin/macgyverctl");
			targetFile.getParentFile().mkdirs();

			try (FileOutputStream fos = new FileOutputStream(targetFile)) {
				ByteStreams.copy(applicationContext.getResource("classpath:templates/macgyverctl").getInputStream(),
						fos);
			}

			targetFile.setExecutable(true);

			String[] env = new String[] { "JAVA_HOME=" + System.getProperty("java.home") };

			Process p = Runtime.getRuntime().exec(targetFile.getAbsolutePath(), env);

			int rc = p.waitFor();
			logger.info("return code from start script: {}", rc);

		} catch (IOException | InterruptedException e) {
			logger.error("problem", e);
		}

	}

}
