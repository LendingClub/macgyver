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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
	Map<String,Command> commandMap = Maps.newHashMap();
	String[] args;

	ConfigManager configManager = new ConfigManager();

	public static final String DEFAULT_PACKAGE="io.macgyver.cli.command";
	public static final String EXTENSION_PACKAGE="io.macgyver.plugin.cli";

	public CLI() {

	}

	List<Class<? extends Command>> findCommands() {
		try {
			List<Class<? extends Command>> classList = Lists.newArrayList();

			List<ClassInfo> classInfoList = Lists.newArrayList();

			classInfoList.addAll(ClassPath.from(Thread.currentThread().getContextClassLoader())
					.getTopLevelClasses(DEFAULT_PACKAGE));

			classInfoList.addAll(ClassPath.from(Thread.currentThread().getContextClassLoader())
					.getTopLevelClasses(EXTENSION_PACKAGE));

			classInfoList.forEach(it -> {
						try {

							Class clazz = Class.forName(it.getName());


							if (Command.class.isAssignableFrom(clazz) ) {
								if (Modifier.isAbstract(clazz.getModifiers())) {
									logger.debug("{} is abstract",clazz);
								}
								else {
									logger.debug("adding {}",clazz);
									classList.add((Class<? extends Command>) clazz);
								}

							}
						} catch (ClassNotFoundException e) {
							logger.warn("", e);
						}
					});
			return classList;
		} catch (IOException e) {
			throw new CLIException(e);
		}
	}

	void buildCommands() {

		findCommands().forEach(c-> {
			try {
				Command command = c.newInstance();
				commandMap.put(command.getCommandName(),command);
				commander.addCommand(command.getCommandName(),command);
				command.cli = this;
				logger.info("adding command: {}",command);
			}
			catch (IllegalAccessException | InstantiationException e) {
				logger.warn("could not load command: "+c,e);
			}

		});

	}

	public Map<String,Command> getCommandMap() {
		return ImmutableMap.copyOf(commandMap);
	}
	public ConfigManager getConfigManager() {
		return configManager;
	}
	public String getCommandName() {
		return commander.getParsedCommand();
	}
	public void run(String ...v) throws IOException {

		configureLogging();

		args = v;

		configManager.loadConfig();

		buildCommands();

		commander.parse(args);

		String commandName = getCommandName();
		logger.info("command name: {}",commandName);

		if (commandName==null) {
			throw new CLIException("no command specified");
		}
		Command command = commandMap.get(commandName);
		logger.info("Command invocked:{} with args {}", command.getCommandName(), args[0]);
		JCommander jCommander = new JCommander();
		jCommander.addObject(command);
    	jCommander.parse(args);
        jCommander.setProgramName(this.getCommandName());

        if (command.getHelp()) {
        	System.out.println("Help Called:"+ command.getHelp());
        	jCommander.usage();
        	return;
        }
		command.execute();
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

	public static boolean isCommandLineLoggingEnabled() {
		return System.getProperty("cli.launch", "false").equals("true");
	}
	public static void configureLogging() {

		if (!isCommandLineLoggingEnabled()) {
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
	public static void main(String[] args) throws IOException {

		boolean debug = true;

		try {
			CLI m = new CLI();

			m.run(args);

		} catch (CLIException  | ParameterException e) {
			logger.debug("",e);

			System.err.println("error: " + e.getMessage());

		}

	}

}
