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


public class LogMessage extends MacGyverMessage {
	String label = null;
	boolean sent = false;
	EventLogger eventLogger;
	
	public LogMessage(EventLogger eventLogger) {
		super();
		withEventType(LogMessage.class.getName());
		this.eventLogger = eventLogger;
	}
	public LogMessage withLabel(String label) {
		this.label = label;
		return this;
	}
	public String getLabel() {
		return label;
	}

	public LogMessage withMessage(String msg) {
		return (LogMessage) withAttribute("message", msg);
	}

	public void log() {
		eventLogger.logEvent(this);
	}
	
}
