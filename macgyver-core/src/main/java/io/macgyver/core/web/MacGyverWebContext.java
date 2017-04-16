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
package io.macgyver.core.web;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpRequest;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class MacGyverWebContext {

	ApplicationContext applicationContext;
	ServletContext servletContext;
	HttpServletRequest request;
	HttpServletResponse response;
	static final ThreadLocal<MacGyverWebContext> contextLocal = new ThreadLocal<>();

	public MacGyverWebContext(ApplicationContext context, ServletContext servletContext,
			HttpServletRequest request, HttpServletResponse response) {
		Preconditions.checkNotNull(context);
		Preconditions.checkNotNull(servletContext);
		Preconditions.checkNotNull(request);
		Preconditions.checkNotNull(response);
		
		this.applicationContext = context;
		this.servletContext = servletContext;
		this.request = request;
		this.response = response;
		
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}
	public ServletContext getServletContext() {
		return servletContext;
	}
	public HttpServletRequest getServletRequest() {
		return request;
	}
	public HttpServletResponse getServletResponse() {
		return response;
	}
	public static MacGyverWebContext get() {
		return contextLocal.get();
	}
	public String toString() {
		return MoreObjects.toStringHelper(this).add("request", request).add("response", response).add("applicationContext", applicationContext).toString();
	}
}
