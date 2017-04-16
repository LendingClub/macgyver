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

import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public class InternalAuthenticationProvider implements AuthenticationProvider {

	org.slf4j.Logger logger = LoggerFactory
			.getLogger(InternalAuthenticationProvider.class);

	@Autowired(required=true)
	UserManager userManager;


	GrantedAuthoritiesMapper grantedAuthoritiesMapper;

	public void setAuthoritiesMapper(GrantedAuthoritiesMapper mapper) {
		this.grantedAuthoritiesMapper = mapper;
	}

	@Override
	public Authentication authenticate(final Authentication authentication)
			throws AuthenticationException {

		Optional<User> u = Optional.absent();
		u = userManager.getInternalUser(authentication.getPrincipal().toString());

		if (!u.isPresent()) {
			throw new UsernameNotFoundException("user not found: "+authentication.getPrincipal().toString());
		}
		boolean b = userManager.authenticate(authentication.getPrincipal()
				.toString(), authentication.getCredentials().toString());
		if (!b) {
			throw new BadCredentialsException("invalid credentials");
		}

		List<GrantedAuthority> gaList = Lists.newArrayList();
		for (String role: u.get().getRoles()) {

			GrantedAuthority ga = new SimpleGrantedAuthority(role);
			gaList.add(ga);
		}




		UsernamePasswordAuthenticationToken upt = new UsernamePasswordAuthenticationToken(
				authentication.getPrincipal().toString(), authentication
						.getCredentials().toString(), gaList);
		return upt;

	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class
				.isAssignableFrom(authentication);

	}



}
