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
package io.macgyver.core.event;

import javax.annotation.PostConstruct;

import org.lendingclub.reflex.operator.ExceptionHandlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.macgyver.core.util.JsonNodes;

public class Slf4jEventWriter {

	Logger logger = LoggerFactory.getLogger(Slf4jEventWriter.class);
	@Autowired
	EventSystem eventSystem;

	@PostConstruct
	public void subscribe() {
		eventSystem.createObservable(MacGyverMessage.class).subscribe(ExceptionHandlers.safeConsumer(x-> {
			if (logger.isDebugEnabled()) {
				logger.debug("logging event:\n {}",JsonNodes.pretty(((MacGyverMessage)x).getEnvelope()));
			}
		}));
	}
}
