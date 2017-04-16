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
package io.macgyver.plugin.elb.a10;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.jdom2.Element;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.squareup.okhttp.Response;

public class RequestBuilder {

	private A10ClientImpl client;
	
	
	private Map<String,String> args = Maps.newHashMap();
	
	private Element xmlBody = null;
	private JsonNode jsonBody = null;
	
	protected RequestBuilder(A10ClientImpl client, String method) {
		this.client = client;
		method(method);
	}
	public RequestBuilder method(String method) {
		Preconditions.checkNotNull(method);
		args.put("method", method);
		return this;
	}
	
	public RequestBuilder params(Map<String,String> args) {
		if (args!=null) {
			this.args.putAll(args);
		}
		return this;
	}
	public Map<String,String> getParams() {
		return Collections.unmodifiableMap(args);
	}
	public RequestBuilder params(String ... args) {
		if (args!=null) {
			this.args.putAll(A10ClientImpl.toMap(args));
		}
		return this;
	}
	public RequestBuilder param(String key, String val) {
		Preconditions.checkArgument(key!=null, "param name cannot be null");
		Preconditions.checkArgument(val!=null, "param name cannot be null");
		args.put(key, val);
		return this;
	}
	public RequestBuilder body(Element e) {
		xmlBody = e;
		jsonBody = null;
		return this;
	}
	public RequestBuilder body(JsonNode n) {
		jsonBody = n;
		xmlBody = null;
		return this;
	}
	
	public String getMethod() {
		return args.get("method");
	}
	public Response execute() {
		return client.execute(this);

	}
	public Element executeXml() {
		return client.executeXml(this.withXmlRequest());
	}
	public ObjectNode executeJson() {
		return client.executeJson(this.withJsonRequest());
	}
	public boolean hasBody() {
		return getXmlBody().isPresent() || getJsonBody().isPresent();
	}

	public RequestBuilder withJsonRequest() {
		return param("format","json");
	}
	public RequestBuilder withXmlRequest() {
		return param("format","xml");
	}
	
	protected Optional<JsonNode> getJsonBody() {
		return Optional.ofNullable(jsonBody);
	}
	protected Optional<Element> getXmlBody() {
		return Optional.ofNullable(xmlBody);
	}
}
