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
package io.macgyver.plugin.elb.a10;

import java.util.Map;

import org.jdom2.Element;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public interface A10Client {


	@Deprecated
	public abstract ObjectNode invoke(String method, Map<String, String> params);

	@Deprecated
	public abstract ObjectNode invoke(String method, String... args);
	
	public abstract ObjectNode invokeJson(String method, JsonNode body, Map<String, String> params);

	public abstract ObjectNode invokeJson(String method, Map<String, String> params);
	
	public abstract String invokeJsonReturnString(String method, JsonNode body, Map<String, String> params);

	public abstract String invokeJsonReturnString(String method, Map<String, String> params);
	
	public abstract ObjectNode invokeJson(String method, JsonNode body, String... args);
	
	public abstract ObjectNode invokeJson(String method, String... args);
	
	public abstract String invokeJsonReturnString(String method, JsonNode body, String... args);
	
	public abstract String invokeJsonReturnString(String method, String... args);
	
	public abstract Element invokeXml(String method, Element body, String... args);
	
	public abstract Element invokeXml(String method, String... args);
	
	public abstract Element invokeXml(String method, Element body, Map<String,String>  args);

	public abstract Element invokeXml(String method, Map<String,String>  args);
	
	public boolean isActive();
	
}
