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

import java.util.EventObject;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;

import com.google.common.base.Optional;

public class Kernel implements ApplicationContextAware {

	static Logger logger = LoggerFactory.getLogger(Kernel.class);

	static AtomicReference<Kernel> kernelRef = new AtomicReference<Kernel>();

	private ApplicationContext applicationContext;

	private static Throwable startupError = null;

	public Kernel() {

	}

	public static Optional<Throwable> getStartupError() {
		return Optional.fromNullable(startupError);
	}

	public static void registerStartupError(Throwable t) {
		if (t != null) {
			startupError = t;
		}
	}

	public boolean isRunning() {
		return startupError == null;
	}

	public synchronized static Kernel getInstance() {
		Kernel k = kernelRef.get();
		if (k == null) {
			throw new IllegalStateException("Kernel not yet initialized");

		}
		return k;
	}


	public synchronized ApplicationContext applicationContext() {

		if (applicationContext == null) {

			throw new IllegalStateException(
					"Kernel's ApplicationContext not initialized");

		}
		return applicationContext;
	}
	
	public static org.lendingclub.neorx.NeoRxClient getNeoRxClient() {
		return Kernel.getApplicationContext().getBean(org.lendingclub.neorx.NeoRxClient.class);
	}
	/*public static NeoRxClient getNeoRxClient() {
		return Kernel.getApplicationContext().getBean(NeoRxClient.class);
	}*/
	public static ApplicationContext getApplicationContext() {
		return Kernel.getInstance().applicationContext();
	}
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		if (this.applicationContext != null
				&& this.applicationContext != applicationContext) {
		
			throw new IllegalStateException("application context already set: "
					+ this.applicationContext + " ;new: " + applicationContext);
		}
		this.applicationContext = applicationContext;
		kernelRef.set(this);

	}

	static Optional<String> profile = null;

	public static synchronized Optional<String> getExecutionProfile() {
		if (profile != null) {
			return profile;
		}
		
		
		Environment standardEnvironment =Kernel.getApplicationContext().getEnvironment();

	
		String[] activeProfiles = standardEnvironment.getActiveProfiles();
		if (activeProfiles == null) {
			profile = Optional.absent();
		}
		for (String p : activeProfiles) {
			if (p != null && p.endsWith("_env")) {
				p = p.substring(0, p.length() - "_env".length());
				profile = Optional.of(p);
			}
		}
		if (profile == null) {
			profile = Optional.absent();
		}

		logger.info("macgyver profile: {}", profile.or("none"));
		return profile;

	}

	public static class ServerStartedEvent extends EventObject {
		public ServerStartedEvent(Kernel source) {
			super(source);
		}
	}

	public static class KernelStartedEvent extends EventObject {

		public KernelStartedEvent(Kernel source) {
			super(source);
		}

	}
}
