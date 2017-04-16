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
package io.macgyver.plugin.hipchat;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.HtmlUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import io.macgyver.plugin.chat.ChatBotContext;
import io.macgyver.plugin.hipchat.HipChatClient.Color;
import io.macgyver.plugin.hipchat.HipChatClient.Format;

public class HipChatBotContext extends ChatBotContext {

	Logger logger = LoggerFactory.getLogger(HipChatBotContext.class);
	
	HipChatClient client;

	JsonNode rawMessage;
	
	List<String> args;
	
	String command;
	public HipChatBotContext(HipChatBot bot, JsonNode n) {
		super(bot);
	
		this.rawMessage = n;
		this.client = bot.getHipChatClient();
		List<String> tmp = Lists.newArrayList(Splitter.onPattern("\\s").trimResults().omitEmptyStrings().splitToList(getRawMessage()));
		if (tmp.size()>0) {
			command = tmp.get(0);
			if (command.startsWith("/")) {
				command = command.substring(1);
				tmp.remove(0);
				args = tmp;
			}
			else {
				throw new IllegalArgumentException("not a command: "+n);
			}
			
		}
		else {
			throw new IllegalArgumentException("not a command: "+n);
		}
		
	}

	@Override
	public String getCommand() {
		return command;
	}

	@Override
	public List<String> getCommandArgs() {
		return args;
	}

	@Override
	public void respond(String response) {
		logger.info("responding to room {} with: {}",getRoomId(),response);
		client.sendRoomNotification(getRoomId(), response);
	}

	@Override
	public String getRoomId() {
		return rawMessage.path("item").path("room").path("id").asText();
	}

	@Override
	public String getRawMessage() {
		return rawMessage.path("item").path("message").path("message").asText();
	}

	@Override
	public String getSenderName() {
		return rawMessage.path("item").path("message").path("from").path("name").asText();
	}
		
	@Override
	public String getSenderId() {
		return rawMessage.path("item").path("message").path("from").path("id").asText();
	}
}
