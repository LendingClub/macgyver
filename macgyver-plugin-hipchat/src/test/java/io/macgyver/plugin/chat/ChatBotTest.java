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
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

public class ChatBotTest {

	public class TestChatBot extends ChatBot {

		@Override
		public ChatBotContext createContext(ChatBot bot, JsonNode n) {
			ChatBotContext c = new ChatBotContext(this) {
				
				@Override
				public void respond(String response) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public String getRoomId() {
					// TODO Auto-generated method stub
					return null;
				}
				
				@Override
				public String getRawMessage() {
					// TODO Auto-generated method stub
					return null;
				}
				
				@Override
				public String getCommand() {
					return "foo";
				}
				
				@Override
				public List<String> getCommandArgs() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String getSenderName() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String getSenderId() {
					// TODO Auto-generated method stub
					return null;
				}
			};
			return c;
		}

		
		
	}
 	@Test
 	public void testIt() {
 		
 		
 		TestChatBot b = new TestChatBot();
 		
 		ChatBotCommand cmd = new ChatBotCommand("foo") {

			@Override
			public void handle(ChatBotContext ctx) {
				// TODO Auto-generated method stub
				
			}
 			
 		};
 		
 		b.register(cmd);
 		
 		Optional<ChatBotCommand> x = b.findCommand("foo");
 		
 		Assertions.assertThat(x.isPresent()).isTrue();
 		
 		Assertions.assertThat(x.get()).isSameAs(cmd);
 		
 	}
	
}
