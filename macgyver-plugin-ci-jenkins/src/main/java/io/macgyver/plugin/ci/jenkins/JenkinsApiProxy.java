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
package io.macgyver.plugin.ci.jenkins;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;

import io.macgyver.core.service.ServiceDefinition;
import io.macgyver.core.service.ServiceRegistry;
import okhttp3.Credentials;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

@Controller
@RequestMapping("/api/jenkins/proxy")
@PreAuthorize("hasAnyRole('ROLE_MACGYVER_USER')")
public class JenkinsApiProxy {

	ObjectMapper mapper = new ObjectMapper();

	Logger logger = LoggerFactory.getLogger(JenkinsApiProxy.class);

	@Autowired
	ServiceRegistry serviceRegistry;

	OkHttpClient client;

	Set<String> methodsWithBody = Sets.newHashSet("POST", "PATCH", "PUT");

	List<AuthorizationFunction> authorizationFuncations = Lists.newCopyOnWriteArrayList();

	public static final String TARGET_URL = "target.url";
	public static final String JENKINS_SERVICE_NAME_ATTR = "request.service.name";
	public static final String REQUEST_URL_ATTR = "request.url";
	static ThreadLocal<HttpServletRequest> threadLocal = new ThreadLocal<>();

	public static abstract class AuthorizationFunction implements Function<HttpServletRequest, Optional<Boolean>> {

		public HttpServletRequest getServletRequest() {
			return threadLocal.get();
		}

		protected String getJenkinsServiceName() {
			return (String) getServletRequest().getAttribute(JENKINS_SERVICE_NAME_ATTR);
		}

	}

	public JenkinsApiProxy() {
		client = new OkHttpClient.Builder().followRedirects(true).build();
	}

	protected boolean hasBody(HttpServletRequest request) {
		return methodsWithBody.contains(request.getMethod().toUpperCase());
	}

	@RequestMapping("/{service}/**")
	public void proxy(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("service") String serviceName) throws IOException {

		try {
			threadLocal.set(request);

			String requestUrl = request.getRequestURL().toString();
			String requestStem = "/api/jenkins/proxy/" + serviceName;
			String requestPrePath = requestUrl.substring(0, requestUrl.indexOf(requestStem));

			String requestServiceUrl = requestPrePath + requestStem;

			logger.info("{}: {}", REQUEST_URL_ATTR, requestServiceUrl);
			request.setAttribute(REQUEST_URL_ATTR, requestServiceUrl);
			request.setAttribute(JENKINS_SERVICE_NAME_ATTR, serviceName);

			if (!authorize(request)) {
				response.setStatus(403);
				return;
			}
			Request.Builder rb = buildProxyRequest(serviceName, request);

			Response r = client.newCall(rb.build()).execute();

			proxyResponse(r, request, response);
		} finally {
			threadLocal.remove();
		}

	}

	protected void proxyResponse(Response southResponse, HttpServletRequest northRequest,
			HttpServletResponse northResponse) throws IOException {
		int statusCode = southResponse.code();
		northResponse.setStatus(statusCode);

		Closer closer = Closer.create();
		try {
			Headers h = southResponse.headers();
			for (String name : h.names()) {
				if (name.toLowerCase().equals("content-encoding")) {
					// do nothing
				} else if (name.toLowerCase().equals("content-length")) {
					// do nothing
				} else {
					logger.info("Add header: {}:{}", name, h.get(name));
					northResponse.addHeader(name, h.get(name));
				}
			}

			ResponseBody body = southResponse.body();

			logger.info("jenkins response: {}", northResponse.getStatus());
			String contentType = southResponse.header("content-type");
			if (contentType.contains("json")) {
				String val = rewriteResponseBody(body.string(), northRequest);
				logger.info("body: {}", val);
				JsonNode n = mapper.readTree(val);

				PrintWriter pw = northResponse.getWriter();
				closer.register(pw);

				pw.print(n.toString());

			} else if (contentType.contains("xml")) {

				PrintWriter pw = northResponse.getWriter();
				closer.register(pw);

				String val = rewriteResponseBody(body.string(), northRequest);

				pw.print(val);

			} else {

				OutputStream os = northResponse.getOutputStream();
				closer.register(os);

				InputStream is = body.byteStream();
				closer.register(is);

				ByteStreams.copy(is, os);

			}

		} finally {
			closer.close();
		}

	}

	public String rewriteResponseBody(String body, HttpServletRequest request) {
		String proxyUrl = (String) request.getAttribute(TARGET_URL);
		String northProxyUrl = (String) request.getAttribute(REQUEST_URL_ATTR);

		return body.replace(proxyUrl, northProxyUrl);
	}

	public String concat(String a, String b) {
		while (a.endsWith("/")) {
			a = a.substring(0, a.length() - 1);
		}

		while (b.startsWith("/")) {
			b = b.substring(1);
		}

		return a + "/" + b;
	}

	Optional<String> getAuthorizationHeaderForService(String serviceName) {
		ServiceDefinition def = serviceRegistry.getServiceDefinitions().get(serviceName);

		if (def == null) {
			throw new IllegalArgumentException("unknown serivce name: " + serviceName);
		}

		String username = def.getProperty("username");
		String password = def.getProperty("password");
		if (username != null && password != null) {
			return Optional.of(Credentials.basic(username, password));
		}
		return Optional.empty();
	}

	protected String lookupTargetUrlForService(String serviceName) {
		
		Preconditions.checkState(serviceRegistry!=null, "ServiceRegistry must be set");
		ServiceDefinition def = serviceRegistry.getServiceDefinitions().get(serviceName);

		if (def == null) {
			throw new IllegalArgumentException("unknown serivce name: " + serviceName);
		}
		String url = def.getProperty("url");

		if (url == null) {
			throw new IllegalStateException("url not set form jenkins service '" + serviceName + "'");
		}

		return url;

	}

	public String toProxyUrl(String name, HttpServletRequest request) {

		String requestPath = request.getRequestURI();
		String stem = "/api/jenkins/proxy/" + name;
		
		Preconditions.checkArgument(requestPath.length()>=stem.length(), "request path must be beneath /api/jenkins/proxy");
		requestPath = requestPath.substring(stem.length());

		if (!requestPath.startsWith("/")) {
			requestPath = "/" + requestPath;
		}

		String targetUrl = lookupTargetUrlForService(name);
		request.setAttribute(TARGET_URL, targetUrl);

		String fullUrl = concat(targetUrl, requestPath);

		return fullUrl;

	}

	protected Request.Builder buildProxyRequest(String serviceName, HttpServletRequest req)
			throws IOException {

		Request.Builder rb = new Request.Builder();

		Optional<String> authHeader = getAuthorizationHeaderForService(serviceName);

		if (authHeader.isPresent()) {
			rb = rb.addHeader("Authorization",
					authHeader.get());
		}
		String url = toProxyUrl(serviceName, req);

		rb = rb.url(url);

		if (hasBody(req)) {
			int contentLength = req.getContentLength();
			ServletInputStream is = req.getInputStream();
			byte[] b = ByteStreams.toByteArray(is);

			RequestBody body = null;
			String contentType = req.getHeader("Content-type");
			if (Strings.isNullOrEmpty(contentType)) {
				body = RequestBody.create(null, b);
			} else {
				body = RequestBody.create(MediaType.parse(contentType), b);
			}
			rb = rb.method(req.getMethod(), body);
		} else {
			rb.method(req.getMethod(), null);
		}

		return rb;
	}

	protected boolean authorize(HttpServletRequest request) {

		Optional<Boolean> rval = Optional.empty();
		for (AuthorizationFunction a : authorizationFuncations) {
			Optional<Boolean> result = a.apply(request);
			if (result.isPresent()) {
				if (!result.get()) {
					logger.info("jenkins proxy authorization failed");
					return false;
				}
				rval = result;
			}
		}

		boolean authorized = rval.orElse(false);

		if (!authorized) {
			logger.info("jenkins proxy authorization failed");
		}
		return authorized;
	}

	public List<AuthorizationFunction> getAuthorizationFunctions() {
		return authorizationFuncations;
	}
	
	public void addAuthorizationFunction(AuthorizationFunction f) {
		Preconditions.checkArgument(f != null, "AuthorizationFunction cannot be null");
		authorizationFuncations.add(f);
	}
}
