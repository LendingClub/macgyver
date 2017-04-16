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



import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import io.macgyver.okrest3.OkRestTarget;

public interface HipChatClient {

	public enum Color {
		YELLOW("yellow"),
		GREEN("green"),
		RED("red"),
		PURPLE("purple"),
		GRAY("gray");
		
		String val;
		private  Color(final String text) {
	        this.val = text;
	    }

	  
	    @Override
	    public String toString() {
	        return val;
	    }
	}
	
	public enum Format {
		HTML("html"),
		TEXT("text");
		
		
		String val;
		private  Format(final String text) {
	        this.val = text;
	    }

	  
	    @Override
	    public String toString() {
	        return val;
	    }
	}
	public OkRestTarget getBaseTarget();

	public JsonNode get(String path, Map<String, String> params);

	public JsonNode get(String path, String... args);

	public void post(String path, JsonNode body);

	public void put(String path, JsonNode body);

	public void delete(String path);
	
	public void sendRoomNotification(String roomId, String message);
	
	public void sendRoomNotification(String roomId, String message, String format, String color, boolean notify);
	
	public void sendRoomNotification(String roomId, String message, Format format, Color color, boolean notify);
}
