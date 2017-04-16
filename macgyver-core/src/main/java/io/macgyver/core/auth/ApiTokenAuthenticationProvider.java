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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.macgyver.core.auth;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import org.lendingclub.neorx.NeoRxClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.beust.jcommander.internal.Sets;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.lambdaworks.crypto.SCryptUtil;

import rx.exceptions.OnErrorNotImplementedException;

public class ApiTokenAuthenticationProvider implements AuthenticationProvider {

	public static final String API_KEY_HEADER_NAME = "X-API-KEY";

	ObjectMapper mapper = new ObjectMapper();
	@Inject
	NeoRxClient neo4j;

	Logger logger = LoggerFactory.getLogger(ApiTokenAuthenticationProvider.class);

	@Value("${API_TOKEN_TTL_SECS:604800}")
	long tokenTTLSeconds = TimeUnit.DAYS.toSeconds(7);

	@Value("${API_TOKEN_MAX_PER_USER:128}")
	int maxTokensPerUser = 128;

	@Override
	public Authentication authenticate(Authentication auth) throws AuthenticationException {

		try {
			return validateToken(Objects.toString(auth.getCredentials(), null)).orElse(null);

		} catch (RuntimeException e) {
			throw new AuthenticationCredentialsNotFoundException("could not validate token", e);
		}

	}

	@Override
	public boolean supports(Class<?> auth) {
		return ApiTokenAuthentication.class.isAssignableFrom(auth);
	}

	Optional<Authentication> validateToken(String token) {
		
		
		if (Strings.isNullOrEmpty(token)) {
			return Optional.empty();
		}
		AtomicReference<Authentication> auth = new AtomicReference<>(null);

		try {
			ApiToken apiToken = ApiToken.parse(token);

			String cypher = "match (a:ApiToken {accessKey:{accessKey}}) return a";

			neo4j.execCypher(cypher, "accessKey", apiToken.getAccessKey()).forEach(it -> {
			
				if (it.path("expirationTs").asLong(0) < System.currentTimeMillis()) {
					logger.info("token has expired");
					deleteToken(apiToken);
				} else {
					if (SCryptUtil.check(apiToken.getSecretKey(), it.path("secretKeyHash").asText())) {

						String username = it.path("username").asText();

						// We would really like to be able to look up the roles
						// granted to the token dynamically in the source
						// provider
						// rather than use the roles at grant time. This is
						// tricky.
						List<GrantedAuthority> grantedAuthorities = Lists.newArrayList();
						it.path("roles").elements().forEachRemaining(r -> {
							SimpleGrantedAuthority sga = new SimpleGrantedAuthority(r.asText());
							grantedAuthorities.add(sga);
						});

						UsernamePasswordAuthenticationToken authenticatedUser = new UsernamePasswordAuthenticationToken(
								username, null, grantedAuthorities);
						auth.set(authenticatedUser);
					} else {
						logger.info("invalid token");
					}
				}

			});
		} catch (NoSuchElementException | OnErrorNotImplementedException e) {
			logger.info("invalid token");
		}
		return Optional.ofNullable(auth.get());
	}

	public void deleteToken(ApiToken token) {
		neo4j.execCypher("match (a:ApiToken {accessKey:{accessKey}}) detach delete a", "accessKey",
				token.getAccessKey());
	}

	public void deleteExpiredTokens() {
		neo4j.execCypher(
				"match (a:ApiToken) where a.expirationTs<timestamp() or not exists(a.expirationTs) detach delete a");
	}

	/**
	 * Revokes old tokens for a given user, leaving the most recent N tokens.
	 * 
	 * @param username
	 * @param tokensToKeep
	 */
	public void revokeOldTokens(String username, int tokensToKeep) {

		if (tokensToKeep > 0) {
			// There must be a better way to do this in neo4j in one shot
			AtomicInteger counter = new AtomicInteger(0);
			String cypher = "match (a:ApiToken {username:{username}}) return a.accessKey as accessKey, a.createTs as createTs order by a.createTs desc";
			neo4j.execCypherAsList(cypher, "username", username).forEach(it -> {
				if (counter.getAndIncrement() >= tokensToKeep) {
					String deleteCypher = "match (a:ApiToken {accessKey:{accessKey}}) delete a";
					neo4j.execCypher(deleteCypher, "accessKey", it.path("accessKey").asText());
				}
			});
		}
	}

	String hashSecretKey(ApiToken token) {
		return SCryptUtil.scrypt(token.getSecretKey(), 4096, 8, 1);
	}

	public JsonNode refreshToken(ApiToken token) {
		return refreshToken(token, getTokenTTLSeconds(), TimeUnit.SECONDS);
	}

	public JsonNode createToken() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();

		Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication()
				.getAuthorities();

		Set<String> roles = Sets.newHashSet();

		if (authorities != null) {
			authorities.forEach(it -> {
				roles.add(it.getAuthority());
			});
		}
		long expiration = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(getTokenTTLSeconds());

		return createToken(username, roles, expiration);
	}

	protected JsonNode createToken(String username, Collection<String> roleSet, long expirationTime) {

		ApiToken apiToken = ApiToken.createRandom();

		String bearerToken = apiToken.getArmoredString();

		String hashedSecretKey = hashSecretKey(apiToken);

		ArrayNode roles = mapper.createArrayNode();
		if (roleSet != null) {
			roleSet.forEach(it -> {
				roles.add(it);
			});
		}

		ObjectNode attrs = mapper.createObjectNode();
		attrs.set("roles", roles);
		String cypher = "merge (a:ApiToken {accessKey:{accessKey}})  set a.secretKeyHash={secretKeyHash}, a.createTs=timestamp(), a.username={username},a.expirationTs={expiration}, a+={attrs}  return a";
		JsonNode n = neo4j.execCypher(cypher, "accessKey", apiToken.getAccessKey(), "username", username, "attrs",
				attrs, "expiration", expirationTime, "secretKeyHash", hashedSecretKey).blockingFirst();

		ObjectNode on = (ObjectNode) n;

		on.put("token", bearerToken);
		on.remove("secretKeyHash");
		on.remove("accessKey");

		return n;
	}

	public JsonNode refreshToken(ApiToken oldToken, long duration, TimeUnit unit) {
		try {

			ApiToken newToken = ApiToken.createRandom();

			String cypher = "match (a:ApiToken {accessKey:{accessKey}}) return a";

			JsonNode n = neo4j.execCypher(cypher, "accessKey", oldToken.getAccessKey()).blockingFirst();

			String username = n.path("username").asText();
			
			long oldExpiration = n.path("expirationTs").asLong(0);
			if (oldExpiration < System.currentTimeMillis()) {
				throw new BadCredentialsException("cannot refresh an expired token");
			}
			long newExpiration = System.currentTimeMillis() + unit.toMillis(duration);

			logger.info("refreshing token for user={}...new token will expire at {}",username, new Date(newExpiration));
			cypher = "create (a:ApiToken {accessKey:{accessKey}, secretKeyHash: {secretKeyHash}}) set a.createTs=timestamp(), a.expirationTs={expirationTs}, a.username={username}, a.roles={roles} return a";

			String hash = hashSecretKey(newToken);

			ObjectNode createdNode = (ObjectNode) neo4j
					.execCypher(cypher, "accessKey", newToken.getAccessKey(), "secretKeyHash", hash, "username",
							n.path("username").asText(), "roles", n.path("roles"), "expirationTs", newExpiration)
					.blockingFirst();
		
			createdNode.put("token", newToken.getArmoredString());
			createdNode.remove("accessKey");
			createdNode.remove("secretKey");

			try {
				revokeOldTokens(username, maxTokensPerUser);
			} catch (RuntimeException e) {
				logger.warn("could not prune old tokens", e);
			}

			return createdNode;
		} catch (NoSuchElementException e) {
			throw new BadCredentialsException("token not found");
		}
	}

	public long getTokenTTLSeconds() {
		return tokenTTLSeconds;
	}

	public void setTokenTTLSeconds(long l) {
		tokenTTLSeconds = l;
	}
}
