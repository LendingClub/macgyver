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
package io.macgyver.plugin.splunk;


import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.splunk.Service;

public class TokenRefreshManager {

	long tokenRefreshIntervalSecs = TimeUnit.MINUTES.toSeconds(5);
	
	Logger logger = LoggerFactory.getLogger(TokenRefreshManager.class);
	Timer timer;
	
	WeakHashMap<Service, Object> serviceCache = new WeakHashMap<Service, Object>();

	public class AuthenticatorTask extends TimerTask {

		@Override
		public void run() {
			try {
				reauthenticate();
			} catch (Exception e) {
				logger.error("problem re-authenticating with splunk");
			}

		}

	}
	public TokenRefreshManager () {
		
	}
	
	@PostConstruct
	public void start() {
		timer  = new Timer("splunk token refresh", true);
		logger.info("will refresh splunk tokens every {} seconds",tokenRefreshIntervalSecs);
		timer.schedule(new AuthenticatorTask(), 0, TimeUnit.SECONDS.toMillis(tokenRefreshIntervalSecs));
	}
	protected synchronized void registerService(Service service) {
		serviceCache.put(service, "dummy");
	}

	protected void reauthenticate(Service s) {
		logger.info("reauthenticating Splunk service: "+s);
		s.login();
	}

	protected void reauthenticate() {
		Set<Service> s = null;
		synchronized (this) {
			s = serviceCache.keySet();
		}
		for (Service service : s) {
			try {
				reauthenticate(service);
			} catch (Exception e) {
				logger.warn("problem re-authenticating service", e);
			}
		}
	}
	
}
