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
package io.macgyver.core.auth;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import com.google.common.base.Strings;

public class ApiTokenAuthenticationFilter extends  GenericFilterBean {

	


	Logger logger = LoggerFactory.getLogger(ApiTokenAuthenticationFilter.class);

	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
	
		AtomicBoolean hasToken = new AtomicBoolean(false);
		try {
			
			extractToken((HttpServletRequest)request).ifPresent(it -> {
				SecurityContextHolder.getContext().setAuthentication(it);
				hasToken.set(true);
			});
			

			chain.doFilter(request, response);
		} finally {
			if (hasToken.get()) {
				SecurityContextHolder.getContext().setAuthentication(null);
			}
		}

	}

	@Override
	public void destroy() {

	}

	
	static Pattern AUTH_TOKEN_PATTERN = Pattern.compile("Token\\s+(\\S+)",Pattern.CASE_INSENSITIVE);
	
	static Optional<String> extractTokenString(HttpServletRequest request) {
		
		String apiKey = request.getHeader(ApiTokenAuthenticationProvider.API_KEY_HEADER_NAME);
		if (!Strings.isNullOrEmpty(apiKey)) {
			return Optional.of(apiKey);
		}
		
		String x = request.getHeader("Authorization");
		if (!Strings.isNullOrEmpty(x)) {
			Matcher m = AUTH_TOKEN_PATTERN.matcher(x);
			if (m.matches()) {
				return Optional.of(m.group(1));
			}		
		}
		
	
		
		return Optional.empty();
	}
	static Optional<ApiTokenAuthentication> extractToken(HttpServletRequest request) {
		Optional<String> token = extractTokenString(request);
		if (token.isPresent()) {
			return Optional.of(new ApiTokenAuthentication(token.get()));
		}
		return Optional.empty();
	}
	

}
