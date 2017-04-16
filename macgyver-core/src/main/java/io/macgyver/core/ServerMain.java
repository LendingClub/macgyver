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
package io.macgyver.core;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.autoconfigure.CrshAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.access.event.PublicInvocationEvent;
import org.springframework.web.context.support.ServletRequestHandledEvent;

import com.google.common.collect.Lists;
import com.sun.akuma.JavaVMArguments;

import io.macgyver.core.Kernel.ServerStartedEvent;
import io.macgyver.core.event.EventLogger;
import io.macgyver.core.event.EventSystem;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Simple wrapper to start server.
 *
 * @author rschoening
 *
 */

@Configuration
@ComponentScan(basePackages = { "io.macgyver.config", "io.macgyver.plugin.config", "io.macgyver.core.config" })
@EnableAutoConfiguration(exclude={CrshAutoConfiguration.class})
@EnableSwagger2
public class ServerMain {

	static Logger logger = org.slf4j.LoggerFactory.getLogger(ServerMain.class);

	static boolean daemonized = false;

	public static final AtomicReference<List<String>> serverProcessArgs = new AtomicReference<List<String>>(Lists.newArrayList());
	
	public static void main(String[] args) throws Exception {

		daemonizeIfRequired();

		LoggingConfig.ensureJavaUtilLoggingIsBridged();

		logServerProcessArguments();

		Bootstrap.printBanner();

		if (!daemonized) {
			logger.info("process not daemonized; set -Dmacgyver.daemon=true to daemonize (EXPERIMENTAL)");
		}

		ApplicationListener<ApplicationEvent> defaultListener = new ApplicationListener<ApplicationEvent>() {

			@Override
			public void onApplicationEvent(ApplicationEvent event) {

				/// https://springframework.guru/running-code-on-spring-boot-startup/

				if (event instanceof ServletRequestHandledEvent || event instanceof PublicInvocationEvent) {
					// this will generate crazy logging if we log all servlet
					// events
					// requests
					if (logger.isDebugEnabled()) {
						logger.debug("onApplicationEvent({})", event);
					}
				} else {
					// but it is very nice to have this logging output, so log
					// it at info
					logger.info("onApplicationEvent({})", event);
				}
				

			}
		};
		
		ApplicationListener<ApplicationFailedEvent> failedEventListener = new ApplicationListener<ApplicationFailedEvent>() {

			@Override
			public void onApplicationEvent(ApplicationFailedEvent arg0) {
				logger.error("Application failed to start.  Process will exit.");
				System.exit(99);			
			}
		};
		
		ConfigurableApplicationContext ctx = new SpringApplicationBuilder().sources(ServerMain.class)
				.initializers(new SpringContextInitializer()).listeners(failedEventListener,defaultListener).run(args);

		Environment env = ctx.getEnvironment();

		logger.info("spring environment: {}", env);
		
		ServerStartedEvent serverStartedEvent = new Kernel.ServerStartedEvent(Kernel.getInstance());
		
		Kernel.getApplicationContext().getBean(EventSystem.class).post(serverStartedEvent);
		logger.info("\n\n\n"+  
		Bootstrap.getBannerText()+
		"\n\n"+
		"Ready for Action!\n\n\n");
		
		Kernel.getApplicationContext().getBean(EventLogger.class).event().withMessage("MacGyver server started").log();
		
		

	}

	public static void daemonizeIfRequired() throws Exception {
		String val = System.getProperty("macgyver.daemon", "false");

		boolean daemonize = val.trim().equalsIgnoreCase("true");

		if (daemonize) {
			com.sun.akuma.Daemon d = new com.sun.akuma.Daemon();

			if (d.isDaemonized()) {
				daemonized = true;
				d.init();
			} else {
				if (daemonize) {
					d.daemonize();
					System.exit(0);
				}
			}
		}
	}

	public static void logServerProcessArguments() {
		try {
			if (serverProcessArgs.get()==null) {
				serverProcessArgs.set(JavaVMArguments.current());
			}
			serverProcessArgs.get().forEach(it -> LoggerFactory.getLogger(ServerMain.class).info("jvm arg: " + it));
		}
		catch (Throwable e) {
			// could fail in a platform specific way (windows, etc.)
			logger.warn("problem logging arguments",e);
		}
	}
}
