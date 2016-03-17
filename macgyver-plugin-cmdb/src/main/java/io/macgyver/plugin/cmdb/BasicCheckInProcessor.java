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
package io.macgyver.plugin.cmdb;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;

import io.macgyver.core.util.JsonNodes;
import io.macgyver.core.util.Neo4jPropertyFlattener;

public class BasicCheckInProcessor implements CheckInProcessor {



	@Override
	public void decorate(ObjectNode request) {

	}

	@Override
	public boolean checkAuth(HttpServletRequest r) {
		return true;
	}

	@Override
	public ObjectNode process(HttpServletRequest request) throws IOException{

		ObjectNode data = JsonNodes.mapper.createObjectNode();
		if (request.getMethod().equalsIgnoreCase("GET") || Strings.nullToEmpty(request.getContentType()).toLowerCase().contains("x-www-form-urlencoded")) {
			
			Enumeration<String> t = request.getParameterNames();
			while (t.hasMoreElements()) {
				String key = t.nextElement();
				String val = request.getParameter(key);
				data.put(key, val);
			}
		}
		else if (request.getMethod().equalsIgnoreCase("PUT") || request.getMethod().equalsIgnoreCase("POST")) {
			if (request.getContentType().toLowerCase().contains("json")) {
				try (InputStream is = request.getInputStream()) {
					data = (ObjectNode) JsonNodes.mapper.readTree(is);
				}
				
			}
		}
		
		return data;
	}

}
