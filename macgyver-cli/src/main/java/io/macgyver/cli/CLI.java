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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.macgyver.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.MissingCommandException;
import com.beust.jcommander.ParameterException;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import io.macgyver.cli.command.HelpCommand;

public class CLI {

	static Logger logger = null;

	JCommander commander = new JCommander();

	Map<String, Command> manuallyRegisteredCommands = Maps.newHashMap();
	Map<String, Command> commandMap = Maps.newHashMap();
	String[] args;

	public static boolean consoleLogging = false;
	ConfigManager configManager = new ConfigManager();

	public static final String DEFAULT_PACKAGE = "io.macgyver.cli.command";
	public static final String EXTENSION_PACKAGE = "io.macgyver.plugin.cli";

	public CLI() {

	}

	protected CLI(ConfigManager cm) {
		this.configManager = cm;
	}

	public static List<String> findPackageNamesToSearch() throws IOException {

		List<String> packageList = Lists.newArrayList();

		// packageList.add(DEFAULT_PACKAGE);
		// packageList.add(EXTENSION_PACKAGE);

		Enumeration<URL> list = Thread.currentThread().getContextClassLoader()
				.getResources("META-INF/macgyver-cli-packages");
		while (list.hasMoreElements()) {
			URL url = list.nextElement();

			try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
				List<String> packages = CharStreams.readLines(br);

				for (String pname : packages) {
					pname = pname.trim();
					if (!packageList.contains(pname) && !Strings.isNullOrEmpty(pname)) {
						LoggerFactory.getLogger(CLI.class).info("Adding {} to CLI command search path", pname);
						packageList.add(pname);
					}
				}

			}

		}
		return packageList;

	}

	List<Class<? extends Command>> findCommands() {
		try {
			List<Class<? extends Command>> classList = Lists.newArrayList();

			List<ClassInfo> classInfoList = Lists.newArrayList();

			for (String packageName : findPackageNamesToSearch()) {
				logger.info("Searching pacakge {} for CLI commands", packageName);
				classInfoList.addAll(ClassPath.from(Thread.currentThread().getContextClassLoader())
						.getTopLevelClasses(packageName));
			}

			classInfoList.forEach(it -> {
				try {
					logger.info("attempting to load: {}", it.getName());
					Class clazz = Class.forName(it.getName());

					if (Command.class.isAssignableFrom(clazz)) {
						if (Modifier.isAbstract(clazz.getModifiers())) {
							logger.debug("{} is abstract", clazz);
						} else {
							logger.debug("adding {}", clazz);
							classList.add((Class<? extends Command>) clazz);
						}

					}
				} catch (ClassNotFoundException | NoClassDefFoundError e) {
					logger.warn("", e);
				}
			});
			return classList;
		} catch (IOException e) {
			throw new CLIException(e);
		}
	}

	void buildCommands() {

		findCommands().forEach(c -> {
			try {
				Command command = c.newInstance();
				commandMap.put(command.getCommandName(), command);
				commander.addCommand(command.getCommandName(), command);
				command.cli = this;
				logger.info("adding command: {}", command);
			} catch (IllegalAccessException | InstantiationException e) {
				logger.warn("could not load command: " + c, e);
			}

		});

		manuallyRegisteredCommands.entrySet().forEach(it -> {
			logger.info("adding manually registred command: {}", it.getKey(), it.getValue());
			commandMap.put(it.getKey(), it.getValue());
		});
	}

	public Map<String, Command> getCommandMap() {
		return commandMap;
	}

	public ConfigManager getConfigManager() {
		return configManager;
	}

	public String getCommandName() {
		
		return commander.getParsedCommand();
	}

	public void addCommand(String name, Command c) {
		manuallyRegisteredCommands.put(name, c);
		c.cli = this;
	}

	public boolean run(String... v) throws IOException {

		configureLogging();

		args = v;

		configManager.loadConfig();

		buildCommands();

		try {
			
			if (v.length>0) {
				String primary = v[0];
				Command primaryCommand = commandMap.get(primary);
				if (primaryCommand!=null && primaryCommand instanceof MetaCommand) {
					if (args.length<2) {
						getCommandMap().get("help").execute();
						return false;
					}
					
					String [] shifted = new String[args.length-1];
					System.arraycopy(args, 1, shifted, 0, args.length-1);
					String subCommandName = args[0]+"-"+args[1];
					shifted[0]=subCommandName;
					logger.info("sub-command: {}",shifted[0]);
					args = shifted;
				}
			}
		
			commander.parse(args);
			
			String commandName = getCommandName();
			logger.info("command name: {}", commandName);

			if (commandName == null) {
				throw new MissingCommandException("");
			}
			Command command = commandMap.get(commandName);
		
			logger.info("Command invoked:{} with args {}", command.getCommandName(), args[0]);

			if (command.isHelpRequested()) {
				throw new ParameterException("");
			}
			commander.addObject(command);
			commander.setProgramName(this.getCommandName());
			command.execute();
			return true;

		} catch (MissingCommandException e) {
			getCommandMap().get("help").execute();
			return false;
		} catch (ParameterException | CLIUsageException e) {
			
			if (e instanceof ParameterException) {
				System.err.println("Error: "+((ParameterException)e).getMessage());
				System.err.println();
			}
			
			
			JCommander jCommander = new JCommander();
			Command command = commandMap.get(getCommandName());
			if (command != null) {
				jCommander.addObject(command);
				jCommander.setProgramName(this.getCommandName());

				jCommander.usage();
			}
			else {
				getCommandMap().get("help").execute();
			}
			return false;
		}
	}

	public JCommander getCommander() {
		return commander;
	}

	String toClassName(String commandName) {
		boolean upper = true;
		StringBuffer sb = new StringBuffer();
		for (char c : commandName.toCharArray()) {
			if (upper) {
				c = Character.toUpperCase(c);
				upper = false;
			}
			if (Character.isAlphabetic(c)) {

				sb.append(c);
			} else {
				upper = true;
			}

		}

		return Command.class.getPackage().getName() + "." + sb.toString() + "Command";
	}

	public static boolean isConsoleLoggingEnabled() {
		if (consoleLogging) {
			return true;
		}
		
		if (System.getProperty("cli.launch", "false").equals("true")) {
			return false;
		}
		
		
		return true;
	}

	public static void configureLogging() {

		if (isConsoleLoggingEnabled()) {
			logger = LoggerFactory.getLogger(CLI.class);
		} else {

			LoggerContext logCtx = (LoggerContext) LoggerFactory.getILoggerFactory();
			LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
			ch.qos.logback.classic.Logger myLogger = loggerContext
					.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
			myLogger.detachAndStopAllAppenders();

			logger = myLogger;
			PatternLayoutEncoder encoder = new PatternLayoutEncoder();
			encoder.setContext(loggerContext);
			encoder.setPattern("%d{HH:mm:ss.SSS} [%-5level] %msg %n");
			encoder.start();

			FileAppender<ILoggingEvent> fileAppender = new FileAppender<ILoggingEvent>();
			fileAppender.setContext(loggerContext);
			fileAppender.setName("MY_FILE_LOGGER");
			fileAppender.setAppend(true);
			fileAppender.setEncoder(encoder);

			File f = new File(System.getProperty("user.home", "."), ".macgyver/macgyver.log");

			fileAppender.setFile(f.getAbsolutePath());
			fileAppender.start();
			myLogger.addAppender(fileAppender);
		}
	}

	public static void main(String[] args) {

		for (String arg: args) {
			if (arg.equals("--debug")) {
				consoleLogging=true;
			}
		}
		int rc = 0;
		try {
			CLI m = new CLI();

			boolean b = m.run(args);

			rc = b ? 0 : 1;

		} catch (CLIUsageException e) {
			System.err.println("invalid usage");
			rc = 1;

		} catch (CLIException e) {
			System.err.println("error: " + e.getMessage());
			rc = 1;

		} catch (IOException | RuntimeException e) {
			logger.error("unexpected exception", e);
			rc = 1;
			System.err.println("error: " + e.toString());

		}

		logger.info("Exiting with rc={}", rc);
		System.exit(rc);

	}

}
