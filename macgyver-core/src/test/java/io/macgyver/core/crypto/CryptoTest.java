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
package io.macgyver.core.crypto;

import io.macgyver.test.MacGyverIntegrationTest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.BaseEncoding;

public class CryptoTest extends MacGyverIntegrationTest {

	ObjectMapper mapper = new ObjectMapper();
	
	@Autowired
	Crypto crypto;

	@Test
	public void testIt() throws GeneralSecurityException {
		String plainTextInput = "Hello!";
		String cipher1 = crypto.encryptString(plainTextInput, "mac0");

		String cipher2 = crypto.encryptString(plainTextInput, "mac0");
		Assert.assertFalse("encryption should be salted",
				cipher1.equals(cipher2));
		String plainTextOutput = crypto.decryptString(cipher1);
		Assert.assertEquals(plainTextInput, plainTextOutput);

	}

	@Test
	public void testDecryptWithMac0() throws GeneralSecurityException {
	
		String input = "eyJrIjoibWFjMCIsImQiOiJKZXE2SXptUEQrZFNHcEhBWUxLODJYTW5JS2JlQ0czMng5U3g5OTNxaTZzPSJ9";
		
		String plain = crypto.decryptString(input);

		Assert.assertEquals("abcdefg", plain);

	}

	@Test(expected = KeyStoreException.class)
	public void testInvalidKey() throws GeneralSecurityException {
		String cipherEnvelope = crypto.encryptString("test", "invalid");
	}

	@Test
	public void testEncrypt() throws GeneralSecurityException,
			UnsupportedEncodingException, IOException {
		Assert.assertNotNull(crypto);
		String encodedEnvelope = crypto.encryptString("test", "mac0");
		String envelope = new String(BaseEncoding.base64().decode(
				encodedEnvelope), "UTF-8");

		JsonNode obj = mapper.readTree(envelope);

		Assert.assertEquals("mac0", obj.get("k").asText());
		Assert.assertTrue(obj.get("d").asText().length() > 10);
		
		Assert.assertEquals("test",crypto.decryptString(encodedEnvelope));

	}

	@Test(expected = GeneralSecurityException.class)
	public void testFailedDecrypt1() throws GeneralSecurityException {
		String x = mapper.createObjectNode().put("k", "x").put("d","x").toString();
		
		
		crypto.decryptString(x);
	}

	@Test(expected = GeneralSecurityException.class)
	public void testFailedDecrypt2() throws GeneralSecurityException {
		String x = mapper.createObjectNode().put("k", "x").toString();
		crypto.decryptString(x);
	}

	@Test
	public void testPassThrough() {
		String x = "pass me through";
		Assert.assertEquals(x, crypto.decryptStringWithPassThrough(x));

		// Even something that looks like an encrypted envelope should be passed
		// through on failure
		x = mapper.createObjectNode().put("k", "x").put("d", "x")
				.toString();
		Assert.assertEquals(x, crypto.decryptStringWithPassThrough(x));
	}
}
