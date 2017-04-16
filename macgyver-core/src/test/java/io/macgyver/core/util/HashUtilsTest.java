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
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.macgyver.core.util.HashUtils;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.BaseEncoding;


public class HashUtilsTest {

	@Test
	public void testPattern() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		byte [] b = MessageDigest.getInstance("sha1").digest("/a/b".getBytes("UTF-8"));
		String hexVal = BaseEncoding.base16().lowerCase().encode(b);
		
		Assert.assertEquals(hexVal,HashUtils.calculateCompositeId("a","b"));
	}
	
	@Test
	public void testEncoding() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		
		Assert.assertEquals("79cc6964e1c99c9f4da834114b230f7694648b9d",HashUtils.calculateCompositeId("a","","b"));
		Assert.assertEquals("79cc6964e1c99c9f4da834114b230f7694648b9d",HashUtils.calculateCompositeId("a",null,"b"));
	}
	
	@Test
	public void testCaseSensitivity() {
		Assert.assertNotEquals(HashUtils.calculateCompositeId("Hello"),HashUtils.calculateCompositeId("hello"));
	}
}
