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
package io.macgyver.core.util;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CertCheckerTest {

	Logger logger = LoggerFactory.getLogger(CertCheckerTest.class);

	@Test
	public void testX() throws IOException, GeneralSecurityException {
		CertChecker c = new CertChecker();
		URL url = new URL("https://www.google.com");
		ObjectNode n =c.checkCertificates(url.toString());
		
	
		logger.info(new ObjectMapper().writerWithDefaultPrettyPrinter()
				.writeValueAsString(n));
		Assertions.assertThat(n.path("host").asText()).isEqualTo(url.getHost());
		Assertions.assertThat(n.path("url").asText()).isEqualTo(url.toString());
	}

	
	@Test
	public void testInvalidHost() throws IOException, GeneralSecurityException {
		CertChecker c = new CertChecker();
		String urlString = "https://invalid="+UUID.randomUUID().toString();
		URL url = new URL(urlString);
		ObjectNode n = c.checkCertificates(urlString);
		
		logger.info(new ObjectMapper().writerWithDefaultPrettyPrinter()
				.writeValueAsString(n));
		
		JsonNode problem = n.path("problems").path(0);
		Assertions.assertThat(problem.path("type").asText()).isEqualTo("error");
		Assertions.assertThat(problem.path("description").asText()).containsIgnoringCase("UnknownHostException");
		Assertions.assertThat(n.path("host").asText()).isEqualTo(url.getHost());
		Assertions.assertThat(n.path("url").asText()).isEqualTo(url.toString());
	}
	
	@Test
	public void testCannotConnect() throws IOException, GeneralSecurityException {
		CertChecker c = new CertChecker();
		URL url = new URL("https://localhost:43543");
		ObjectNode n = c.checkCertificates(url.toString());
		
		logger.info(new ObjectMapper().writerWithDefaultPrettyPrinter()
				.writeValueAsString(n));
		
		JsonNode problem = n.path("problems").path(0);
		Assertions.assertThat(problem.path("type").asText()).isEqualTo("error");
		Assertions.assertThat(problem.path("description").asText()).containsIgnoringCase("ConnectException");
		
		Assertions.assertThat(n.path("host").asText()).isEqualTo(url.getHost());
		Assertions.assertThat(n.path("url").asText()).isEqualTo(url.toString());
	}
}
