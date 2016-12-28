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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.google.common.base.Optional;

import io.macgyver.core.Kernel.KernelStartedEvent;
import io.macgyver.core.event.EventSystem;
import reactor.bus.Event;
import reactor.bus.EventBus;

public class ContextRefreshApplicationListener implements
		ApplicationListener<ContextRefreshedEvent> {



	@Autowired
	EventBus reactorBus;
	
	@Autowired
	EventSystem eventSystem;
	
	Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {

		log.info("posting lifecycle event to EventSystem: {}", event);
		reactorBus.notify(event,Event.wrap(event)); // deprecated
		eventSystem.publish(event);
		
		Optional<Throwable> e = Kernel.getInstance().getStartupError();

		if (e.isPresent()) {
			if (e.get() instanceof RuntimeException) {
				throw ((RuntimeException) e.get());
			}
			throw new RuntimeException(e.get());
		}
		
		KernelStartedEvent kse = new Kernel.KernelStartedEvent(Kernel.getInstance());
		
		
		reactorBus.notify(event,Event.wrap(kse)); //deprecated
		eventSystem.publish(kse);
		log.info("event post complete");
	}

}
