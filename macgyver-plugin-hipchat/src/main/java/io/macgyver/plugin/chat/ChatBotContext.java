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
package io.macgyver.plugin.chat;

import java.util.List;

import org.joda.time.DateTime;

public abstract class ChatBotContext {

	private ChatBot chatbot;
	
	public ChatBotContext(ChatBot cb) {
		this.chatbot = cb;
	}
	
	
	public abstract String getRawMessage();
	public abstract String getCommand();
	public abstract List<String> getCommandArgs();
	public abstract String getRoomId();
	public abstract String getSenderName();	
	public abstract String getSenderId();
	public abstract void respond(String response);
	public ChatBot getChatBot() {
		return chatbot;
	}
}
