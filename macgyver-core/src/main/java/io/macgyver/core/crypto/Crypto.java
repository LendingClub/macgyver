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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.SecureRandom;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;

public class Crypto {

	Logger logger = LoggerFactory.getLogger(Crypto.class);

	public static Crypto instance;

	KeyStoreManager keyStoreManager = new KeyStoreManager();
	
	ObjectMapper mapper = new ObjectMapper();
	
	public KeyStoreManager getKeyStoreManager() {
		return keyStoreManager;
	}

	public String encryptString(String plainText, String alias)
			throws GeneralSecurityException {
		KeyStore ks = keyStoreManager.getKeyStore();
		SecretKey sk = (SecretKey) ks.getKey(alias,
				keyStoreManager.getPasswordForKey(alias));
		if (sk == null) {
			throw new KeyStoreException("no such key: " + alias);
		}
		return encryptString(plainText, sk, alias);

	}

	protected static String readFirstLine(Reader r) throws IOException {
		BufferedReader br = new BufferedReader(r);
		String line = br.readLine();
		br.close();
		return line;
	}

	protected static byte[] readByteArray(InputStream input) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = input.read(buffer)) > 0) {
			baos.write(buffer, 0, len);
		}
		baos.close();
		return baos.toByteArray();

	}

	protected InputStream decrypt(InputStream encrypted, SecretKey secretKey)
			throws IOException, GeneralSecurityException {

		byte[] b = readByteArray(encrypted);

		byte[] ivdata = new byte[16];

		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		bais.read(ivdata);

		Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");

		c.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(ivdata));

		return new CipherInputStream(bais, c);
	}

	protected String decryptString(String cipherText, SecretKey sk)
			throws GeneralSecurityException {

		try {

			InputStream input = null;

			;
			input = decrypt(new ByteArrayInputStream(BaseEncoding.base64()
					.decode(cipherText)), sk);
			byte[] b = readFully(input);

			String decryptedString = new String(b, "UTF-8");
			if (Strings.isNullOrEmpty(decryptedString)) {

				throw new GeneralSecurityException();
			}
			return decryptedString;
		} catch (IOException e) {
			throw new GeneralSecurityException(e);
		} catch (IllegalArgumentException e) {
			throw new GeneralSecurityException(e);
		}

	}

	public static final String ENCRYPTED_STRING_HEADER="eyJrIjoi";
	public String decryptStringWithPassThrough(String input) {
		try {
			if (input.startsWith(ENCRYPTED_STRING_HEADER)) {
				return decryptString(input);
			}
			return input;
		}
		catch (GeneralSecurityException e) {
			e.printStackTrace();
			return input;
		}
		catch (Exception e) {
			return input;
		}
	}

	/**
	 * Take a string and parse JSON if it is an encrypted envelope.
	 * 
	 * @param input
	 * @return
	 */
	Optional<JsonNode> decodeEnvelope(String input) {

		try {
			input = new String(BaseEncoding.base64().decode(input), "UTF-8");
			if (input == null || input.length() < 2 || input.charAt(0) != '{') {
				return Optional.absent();
			}
			JsonNode obj = mapper.readTree(input);
			
			
			if (obj.has("d") && obj.has("k")) {
				return Optional.of(obj);
			} else {
				return Optional.absent();
			}
		} catch (Exception e) {
			return Optional.absent();
		}

	}

	public String decryptString(String input) throws GeneralSecurityException {

		Optional<JsonNode> envelope = decodeEnvelope(input);
		if (envelope.isPresent()) {
			String keyAlias = envelope.get().get("k").asText();
			logger.debug("decrypting with alias: {}",keyAlias);
			SecretKey key = (SecretKey) keyStoreManager.getKey(keyAlias);
			if (key==null) {
				throw new KeyStoreException("could not load key: "+keyAlias);
			}
			return decryptString(envelope.get().get("d").asText(), key);
		}
		throw new GeneralSecurityException("could not decrypt");

	}

	protected Cipher cipherForKey(SecretKey key)
			throws GeneralSecurityException {

		if (!"AES".equals(key.getAlgorithm())) {
			throw new GeneralSecurityException("key algorithm not supported: "
					+ key.getAlgorithm());
		}
		return Cipher.getInstance("AES/CBC/PKCS5Padding");
	}

	protected String encryptString(String plainText, SecretKey secretKey,
			String alias) throws GeneralSecurityException {
		try {
			Cipher c = cipherForKey(secretKey);

			byte[] ivdata = new byte[16];
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			sr.nextBytes(ivdata);

			// DerivedKey key = deriveKey(pbe, salt,keyLenBits, 16*8);
			IvParameterSpec ivps = new IvParameterSpec(ivdata);
			SecretKeySpec sks = new SecretKeySpec(secretKey.getEncoded(),
					secretKey.getAlgorithm());

			c.init(Cipher.ENCRYPT_MODE, sks, ivps);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			baos.write(ivdata);
			CipherOutputStream cos = new CipherOutputStream(baos, c);
			cos.write(plainText.getBytes("UTF-8"));
			cos.close();

			String encoded = BaseEncoding.base64().encode(baos.toByteArray());
			
			ObjectNode n = mapper.createObjectNode().put("k",alias).put("d", encoded);
			String encodedEnvelope = BaseEncoding.base64().encode(n.toString()
							.getBytes("UTF-8"));
			return encodedEnvelope;

		} catch (IOException e) {
			throw new GeneralSecurityException(e);
		}

	}

	protected byte[] readFully(InputStream input) throws IOException {
		if (input == null) {
			throw new NullPointerException("readFully() cannot accept null");
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1];
		int len = 0;
		while ((len = input.read(buffer)) > 0) {
			baos.write(buffer, 0, len);
		}

		baos.close();
		return baos.toByteArray();
	}

	public Properties decryptProperties(Properties input) {
		
		Preconditions.checkNotNull(input);
		Properties out = new Properties();
		for (Object key : input.keySet()) {
			String val = input.getProperty(key.toString());
			val = decryptStringWithPassThrough(val);
			out.put(key, val);
		}

		return out;
	}

	@PostConstruct
	public void createKeyStoreIfNotPresent() {
		keyStoreManager.createKeyStoreIfNotPresent();
	}
}
