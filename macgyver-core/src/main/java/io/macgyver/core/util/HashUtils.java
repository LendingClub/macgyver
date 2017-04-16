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

import io.macgyver.core.MacGyverException;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;

public class HashUtils {
	public static String calculateCompositeId(String... args) {

		try {
			MessageDigest md = MessageDigest.getInstance("sha1");
			StringBuffer sb = new StringBuffer();
			for (String s : args) {
				sb.append("/");
				sb.append(Strings.nullToEmpty(s));

			}
			md.update(sb.toString().getBytes("UTF-8"));
			return BaseEncoding.base16().lowerCase().encode(md.digest());
		} catch (NoSuchAlgorithmException e) {
			throw new MacGyverException(e);
		} catch (UnsupportedEncodingException e) {
			throw new MacGyverException(e);
		}

	}
}
