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

import io.macgyver.plugin.hipchat.HipChatBot;
import io.macgyver.plugin.hipchat.HipChatBotContext;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HipChatBotContextTest {

	ObjectMapper mapper = new ObjectMapper();
	
	String event = "{\"event\":\"room_message\",\"item\":{\"message\":{\"date\":\"2015-09-12T23:21:14.248263+00:00\",\"from\":{\"id\":112233,\"links\":{\"self\":\"https://api.hipchat.com/v2/user/112233\"},\"mention_name\":\"RobSchoening\",\"name\":\"Rob Schoening\",\"version\":\"00000000\"},\"id\":\"ffffffff-b54d-4c79-bff3-aaaaaaaaaaaa\",\"mentions\":[],\"message\":\"/test arg1 arg2\",\"type\":\"message\"},\"room\":{\"id\":987654,\"links\":{\"members\":\"https://api.hipchat.com/v2/room/987654/member\",\"participants\":\"https://api.hipchat.com/v2/room/987654/participant\",\"self\":\"https://api.hipchat.com/v2/room/987654\",\"webhooks\":\"https://api.hipchat.com/v2/room/987654/webhook\"},\"name\":\"DevOps\",\"version\":\"00000000\"}},\"oauth_client_id\":\"ffffffff-6e1b-472f-9ae9-ffffffffffff\",\"webhook_id\":2222222}";

	
	
	@Test
	public void testIt() throws IOException  {
		JsonNode n = mapper.readTree(event);
		
		
		System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(n));
		HipChatBot bot = Mockito.mock(HipChatBot.class);
		
		HipChatBotContext ctx = new HipChatBotContext(bot, n);
		
		
		Assertions.assertThat(ctx.getRoomId()).isEqualTo("987654");

		Assertions.assertThat(ctx.getRawMessage()).isEqualTo("/test arg1 arg2");
		
		Assertions.assertThat(ctx.getCommand()).isEqualTo("test");
		
		Assertions.assertThat(ctx.getSenderId()).isEqualTo("112233");
		
		Assertions.assertThat(ctx.getSenderName()).isEqualTo("Rob Schoening");
		
	}

}
