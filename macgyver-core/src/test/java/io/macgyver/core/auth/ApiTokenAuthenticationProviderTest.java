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

import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.lendingclub.neorx.NeoRxClient;
import org.rapidoid.u.U;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;

import io.macgyver.test.MacGyverIntegrationTest;

public class ApiTokenAuthenticationProviderTest extends MacGyverIntegrationTest {

	@Autowired
	NeoRxClient neo4j;

	@Autowired
	ApiTokenAuthenticationProvider provider;

	@Test
	public void testIt() {

		provider.deleteExpiredTokens();

		String username = "junit-" + System.currentTimeMillis();
		logger.info("username={}",username);
		JsonNode n = provider.createToken(username, U.set("role_1", "role_2"), System.currentTimeMillis() + 60000);

		ApiToken token = ApiToken
				.parse(n.path("token").asText());

		JsonNode newData = provider.refreshToken(token, 5, TimeUnit.MINUTES);

		ApiToken newToken = ApiToken.parse(newData.path("token").asText());

		Assertions.assertThat(newToken.getAccessKey()).isNotEqualTo(token.getAccessKey());
	
		JsonNode newTokenEntry = neo4j
				.execCypher("match (a:ApiToken {accessKey:{accessKey}}) return a", "accessKey", newToken.getAccessKey())
				.blockingFirst();

		Assertions.assertThat(newTokenEntry.path("accessKey").asText()).isEqualTo(newToken.getAccessKey());
		Assertions.assertThat(newTokenEntry.path("username").asText()).isEqualTo(username);
		Assertions.assertThat(newTokenEntry.path("createTs").asLong(0))
				.isGreaterThan(System.currentTimeMillis() - 5000);

		long approximateExpiration = (System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5)) - 5000;
		Assertions.assertThat(newTokenEntry.path("expirationTs").asLong(0)).isGreaterThan(approximateExpiration);

		
		org.springframework.security.core.Authentication auth = provider.validateToken(newToken.getArmoredString())
				.get();

		Assertions.assertThat(auth.getPrincipal().toString()).isEqualTo(username);
		Assertions.assertThat(auth.isAuthenticated()).isTrue();
		Assertions.assertThat(auth.getAuthorities().size()).isEqualTo(2);
		Assertions.assertThat(auth.getAuthorities().stream().allMatch(p -> {

			return p.getAuthority().equals("role_1") || p.getAuthority().equals("role_2");

		})).isTrue();

		// now forcibly expire the token

		neo4j.execCypher("match (a:ApiToken {accessKey:{accessKey}}) set a.expirationTs=timestamp()-5000", "accessKey",
				newToken.getAccessKey());

		Assertions.assertThat(provider.validateToken(newToken.getArmoredString()).isPresent()).isFalse();

		Assertions.assertThat(
				neo4j.execCypherAsList("match (a:ApiToken {accessKey:{accessKey}}) set a.expirationTs=timestamp()-5000",
						"accessKey", newToken.getAccessKey()).isEmpty())
				.isTrue();

		System.out.println(auth);
	}
}
