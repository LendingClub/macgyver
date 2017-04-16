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

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import io.macgyver.plugin.chat.ChatBotCommand;
import io.macgyver.plugin.chat.ChatBotContext;

public class HelpCommand extends ChatBotCommand {

	public HelpCommand() {
		super("help");
	}

	@Override
	public void handle(ChatBotContext ctx) {

		ctx.respond("Summary of MacGyver HipChat commands:<br/");

		StringBuffer sb = new StringBuffer();
		sb.append("<table></tr>");

		List<ChatBotCommand> tmp = Lists
				.newArrayList(ctx.getChatBot().getCommands().values());
		Collections.sort(tmp, (a, b) -> {
			return a.getCommand().compareTo(b.getCommand());
		});
		tmp
				.forEach(
						it -> {
							sb.append("<tr><td>" + it.getUsage()+
									"</td><td>-</td>"
									+ "<td>"+it.getDescription()
									+ "</td></tr>");

						});

		sb.append("</table>");

		HipChatClient c = ((HipChatBot) ctx.getChatBot()).getHipChatClient();
		c.sendRoomNotification(ctx.getRoomId(), sb.toString());

	}

}
