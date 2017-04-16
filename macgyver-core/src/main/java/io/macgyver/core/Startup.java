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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import io.macgyver.core.auth.InternalAuthenticationProvider;
import io.macgyver.core.resource.Resource;
import io.macgyver.core.script.ExtensionResourceProvider;
import io.macgyver.core.script.ScriptExecutor;
import io.macgyver.core.service.ServiceRegistry;

public class Startup implements ApplicationListener<ContextRefreshedEvent> {

	Logger logger = LoggerFactory.getLogger(Startup.class);



	@Autowired
	Kernel kernel;

	@Autowired
	ServiceRegistry registry;
	
	@Autowired
	InternalAuthenticationProvider internalAuthenticationProvider;

	@Autowired
	ExtensionResourceProvider resourceLoader;
	




	public void runInitScripts() throws IOException {
		
		for (Resource r: resourceLoader.findResources()) {
			if (r.getPath().startsWith("scripts/init/")) {
				runInitScript(r);
			}
		
		}
		

	}

	public void runInitScript(Resource resource) throws IOException {
		
		
	
		ScriptExecutor se = new ScriptExecutor();
		if (se.isSupportedScript(resource)) {
			try {
				se.run(resource, null, false);
			}
			catch (RuntimeException e) {
				kernel.registerStartupError(e);
				throw e;
			}
		} else {
			logger.info("ignoring file in init script dir: {}", resource);
		}
	}



	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		try {
		if (kernel.getApplicationContext() != event.getSource()) {
			return;
		}
		logger.info("STARTED: {}", event);
		
		registry.startAfterSpringContextInitialized();
		runInitScripts();
		}
		catch (IOException e) {
			throw new MacGyverException(e);
		}
		
	}

	
}
