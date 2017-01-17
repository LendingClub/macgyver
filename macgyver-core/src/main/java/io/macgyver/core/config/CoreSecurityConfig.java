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
package io.macgyver.core.config;

import io.macgyver.core.ScriptHookManager;
import io.macgyver.core.auth.GrantedAuthoritiesTranslatorChain;
import io.macgyver.core.auth.GrantedAuthoritiesTranslatorScriptHook;
import io.macgyver.core.auth.InternalAuthenticationProvider;
import io.macgyver.core.auth.InternalGroupRoleTranslator;
import io.macgyver.core.auth.LogOnlyAccessDecisionVoter;
import io.macgyver.core.auth.MacGyverAccessDecisionManager;

import io.macgyver.core.auth.ApiTokenController;
import io.macgyver.core.auth.AuthorizationManager;
import io.macgyver.core.auth.ApiTokenAuthenticationFilter;
import io.macgyver.core.auth.ApiTokenAuthenticationProvider;

import io.macgyver.core.crypto.KeyStoreManager;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.annotation.Jsr250Voter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, proxyTargetClass = true, prePostEnabled = true)
public class CoreSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	InternalAuthenticationProvider internalAuthenticationProvider;

	@Autowired
	ApiTokenAuthenticationProvider tokenAuthenticationProvider;
	
	@Autowired
	ScriptHookManager hookScriptManager;


	@Override
	public void configure(WebSecurity web) throws Exception {

		/*
		 * Map<String, Object> map = Maps.newHashMap(); map.put("webSecurity",
		 * web); hookScriptManager.invokeHook("configureWebSecurity", map);
		 */
	}

	@Override
	protected void configure(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.csrf().disable();

		httpSecurity.authorizeRequests()

		.antMatchers("/login", "/error**", "/public/**", "/resources/**", "/webjars/**").permitAll().and().

		formLogin().loginPage("/login").failureUrl("/login").defaultSuccessUrl("/home").and().logout().permitAll();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {

		auth.authenticationProvider(internalAuthenticationProvider).authenticationProvider(tokenAuthenticationProvider);
		
		Map<String, Object> map = Maps.newHashMap();
		map.put("authBuilder", auth);
		hookScriptManager.invokeHook("configureAuthProviders", map);
	}

	@Autowired
	public void registerSharedAuthentication(AuthenticationManagerBuilder auth) throws Exception {

		auth.authenticationProvider(internalAuthenticationProvider).authenticationProvider(tokenAuthenticationProvider);
		
		Map<String, Object> map = Maps.newHashMap();
		map.put("authBuilder", auth);
		hookScriptManager.invokeHook("configureAuthProviders", map);
	}

	@Configuration
	@Order(92)
	public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.csrf().disable().addFilterBefore(new ApiTokenAuthenticationFilter(), BasicAuthenticationFilter.class).antMatcher("/api/**").httpBasic();
		}
	}

	@SuppressWarnings("rawtypes")
	@Bean
	List<AccessDecisionVoter<? extends Object>> macAccessDecisionVoterList() {
		List<AccessDecisionVoter<? extends Object>> x = Lists.newCopyOnWriteArrayList();
		x.add(new LogOnlyAccessDecisionVoter());
		x.add(new RoleVoter());
		x.add(new WebExpressionVoter());
		x.add(new Jsr250Voter());
		return x;

	}

	@SuppressWarnings("rawtypes")
	@Bean
	AccessDecisionManager macAccessDecisionManager() {

		List<AccessDecisionVoter<? extends Object>> list = macAccessDecisionVoterList();

		return new MacGyverAccessDecisionManager(list);
	}

	@Configuration
	@Order(91)
	public static class ApiPublicWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.antMatcher("/api/public/**").anonymous();

		}
	}

	@Bean
	public InternalGroupRoleTranslator macInternalGroupRoleTranslator() {
		return new InternalGroupRoleTranslator();
	}

	@Bean(name = "macGrantedAuthoritiesTranslatorChain")
	public GrantedAuthoritiesTranslatorChain macGrantedAuthoritiesTranslatorChain() {
		GrantedAuthoritiesTranslatorChain chain = new GrantedAuthoritiesTranslatorChain();
		chain.addTranslator(macInternalGroupRoleTranslator());
		chain.addTranslator(macGrantedAuthoritiesTranslatorScriptHook());
		return chain;
	}

	@Bean
	public GrantedAuthoritiesTranslatorScriptHook macGrantedAuthoritiesTranslatorScriptHook() {
		return new GrantedAuthoritiesTranslatorScriptHook();
	}

	@Bean
	public InternalAuthenticationProvider macInternalAuthenticationProvider() {
		InternalAuthenticationProvider p = new InternalAuthenticationProvider();
		p.setAuthoritiesMapper(macGrantedAuthoritiesTranslatorChain());
		return p;
	}

	@Bean
	public ApiTokenController macTokenController() {
		return new ApiTokenController();
	}



	
	@Bean
	public ApiTokenAuthenticationProvider macTokenAuthenticationProvider() {
		return new ApiTokenAuthenticationProvider();
	}


	@Bean
	public AuthorizationManager macAuthorizationManager() {
		return new AuthorizationManager();
	}
}
