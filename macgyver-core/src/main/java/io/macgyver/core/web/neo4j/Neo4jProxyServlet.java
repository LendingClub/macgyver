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
package io.macgyver.core.web.neo4j;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lendingclub.neorx.NeoRxClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class Neo4jProxyServlet extends HttpServlet {

	public static class UnauthorizedException extends RuntimeException {

		public UnauthorizedException(String message) {
			super(message);

		}

	}

	public static enum Roles {
		ROLE_NEO4J_READ, ROLE_NEO4J_WRITE
	}

	@Autowired
	NeoRxClient neorx;

	

	Logger logger = LoggerFactory.getLogger(Neo4jProxyServlet.class);

	Set<String> methodsWithBody = Sets.newHashSet("POST", "PATCH", "PUT");

	ObjectMapper mapper = new ObjectMapper();

	@Value(value = "${neo4j.url}")
	String neo4jUrl;

	@Value(value = "${neo4j.username:}")
	String neo4jUsername;

	@Value(value = "${neo4j.password:}")
	String neo4jPassword;

	List<Pattern> mutationPatterns = Lists.newCopyOnWriteArrayList();

	public Neo4jProxyServlet() {
		super();
		addMutationCypherKeyword("create");
		addMutationCypherKeyword("set");
		addMutationCypherKeyword("delete");
		addMutationCypherKeyword("remove");
		addMutationCypherKeyword("merge");
	}

	public void addMutationCypherKeyword(String keyword) {
		mutationPatterns.add(Pattern.compile(".*(\\W|^)+" + keyword
				+ "([\\W]+.*|$)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
	}

	boolean isNeo4jProxyWriteAuthorized(HttpServletRequest request) {
		if (request.isUserInRole("ROLE_NEO4J_WRITE")) {
			return true;
		}
		return false;
	}

	boolean isNeo4jProxyReadAuthorized(HttpServletRequest request) {
		if (request.isUserInRole("ROLE_NEO4J_READ")) {
			return true;
		}
		return false;
	}

	boolean isNeo4jProxyAuthorized(HttpServletRequest request) {

		return isNeo4jProxyReadAuthorized(request)
				|| isNeo4jProxyWriteAuthorized(request);

	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		if (req.getRequestURI().equals("/browser")) {
			resp.sendRedirect("/browser/");
			return;
		}

		if (req.getUserPrincipal() == null) {
			resp.sendRedirect("/login/");
			return;
		}

		try {
			if (!isOperationAllowed(req)) {
				throw new UnauthorizedException("not allowed through proxy");
			}
			if (!isNeo4jProxyAuthorized(req)) {
				throw new UnauthorizedException("not authorized");
			}

			Request.Builder rb = buildProxyRequest(req);

			
			//Response r = neorx.getOkHttpClient().newCall(rb.build()).execute();
			//proxyResponse(r, resp);
		
		
		} catch (UnauthorizedException e) {
			resp.sendError(403, e.getMessage());
		}

	}

	protected Request.Builder buildProxyRequest(HttpServletRequest req)
			throws IOException {

		Request.Builder rb = new Request.Builder();

		rb = rb.url(toProxyUrl(req));

		Enumeration<String> headers = req.getHeaderNames();
		while (headers.hasMoreElements()) {
			String headerName = headers.nextElement();
			String headerValue = req.getHeader(headerName);
			rb.header(headerName, headerValue);
		}

		if (hasBody(req)) {
			int contentLength = req.getContentLength();
			ServletInputStream is = req.getInputStream();
			byte[] b = ByteStreams.toByteArray(is);

			checkNeo4jRequest(req, b);
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

	protected boolean hasBody(HttpServletRequest request) {
		return methodsWithBody.contains(request.getMethod().toUpperCase());
	}

	protected void proxyResponse(Response southResponse,
			HttpServletResponse northResponse) throws IOException {
		int statusCode = southResponse.code();
		northResponse.setStatus(statusCode);

		Headers h = southResponse.headers();
		for (String name : h.names()) {
			if (name.toLowerCase().equals("content-encoding")) {
				// do nothing
			} else if (name.toLowerCase().equals("content-length")) {
				// do nothing
			} else {
				northResponse.addHeader(name, h.get(name));
			}
		}

		ResponseBody body = southResponse.body();
		ServletOutputStream os = northResponse.getOutputStream();
		os.write(body.bytes());
		os.close();

	}

	protected String toProxyUrl(HttpServletRequest request) {
		return neo4jUrl + request.getRequestURI();
	}

	protected void checkNeo4jRequest(HttpServletRequest request, byte[] b) {
		if (!request.getRequestURI().startsWith("/db/data")) {
			return;
		}
		JsonNode jsonRequest = null;
		try {
			jsonRequest = mapper.readTree(b);
		} catch (IOException e) {
			// not really a problem if it didn't parse
			return;
		}
		checkNeo4jRequest(request, jsonRequest);

	}

	protected void checkNeo4jRequest(HttpServletRequest servletRequest,
			JsonNode request) {

		for (JsonNode n : Lists.newArrayList(request.path("statements")
				.iterator())) {

			String cypher = n.path("statement").asText();
			logger.info("cypher: {}", cypher);
			if (isNeo4jProxyWriteAuthorized(servletRequest)) {
				// no further checks required
			} else if (isNeo4jProxyReadAuthorized(servletRequest)) {
				if (!isCypherReadOnly(cypher)) {
					logger.warn("mutating cypher not authorized: {}", cypher);
					throw new UnauthorizedException("proxy is read-only");
				}
			} else {
				throw new UnauthorizedException("proxy access not allowed");
			}

		}

	}

	protected boolean isCypherReadOnly(String cypher) {

		for (Pattern p : mutationPatterns) {
			if (p.matcher(cypher).matches()) {
				return false;
			}
		}

		return true;
	}

	protected boolean isOperationAllowed(HttpServletRequest request) {
		String path = request.getRequestURI();

		if (path.startsWith("/db/data")) {
			if (path.equals("/db/data") || path.equals("/db/data/")) {
				return true;
			} else if (path.startsWith("/db/data/transaction")) {
				return true;
			}
			else if (request.getMethod().toUpperCase().equals("GET")) {
				if (path.equals("/db/data/labels") ||
						path.equals("/db/data/relationship/types") ||
						path.equals("/db/data/propertykeys")) {
					return true;
				}
			}
			logger.warn("path not allowed through proxy: {}", path);
			return false;
		}

		return true;
	}
}
