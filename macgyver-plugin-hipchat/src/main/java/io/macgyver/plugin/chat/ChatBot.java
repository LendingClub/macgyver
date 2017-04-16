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

import io.macgyver.plugin.hipchat.HelpCommand;
import io.macgyver.plugin.hipchat.HipChatBotContext;

import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public abstract class ChatBot {

	private Map<String,ChatBotCommand> commandMap = Maps.newConcurrentMap();
	
	
	public ChatBot() {
		register(new HelpCommand());
	}
	
	public void register(ChatBotCommand cmd) {
		this.commandMap.put(cmd.getCommand(), cmd);
	}
	
	
	public abstract ChatBotContext createContext(ChatBot bot, JsonNode n);
	
	public Map<String,ChatBotCommand> getCommands() {
		return ImmutableMap.copyOf(commandMap);
	}
	public final void dispatch(JsonNode n) {
		
		
		ChatBotContext ctx = createContext(this,n);
		
		Optional<ChatBotCommand> cmd = findCommand(ctx);
		
		if (cmd.isPresent()) {
			cmd.get().handle(ctx);
		}
		else {
			String command = ctx.getCommand();
			handleUnknownCommand(ctx);
			
		}
	}
	
	public Optional<ChatBotCommand> findCommand(String cmd) {
		if (cmd == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(commandMap.get(cmd));
	}
	public Optional<ChatBotCommand> findCommand(ChatBotContext ctx) {
		if (ctx==null) {
			return Optional.empty();
		}
	
		return findCommand(ctx.getCommand());
		
		
	}
	
	protected void handleUnknownCommand(ChatBotContext ctx) {
		ctx.respond("unknown command: "+ctx.getCommand()+". Type /help for help with commands");
	}
}
