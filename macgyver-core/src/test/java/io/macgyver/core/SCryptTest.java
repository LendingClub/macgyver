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
package io.macgyver.core;

import org.junit.Assert;
import org.junit.Test;

import com.lambdaworks.crypto.SCryptUtil;

public class SCryptTest {

	String crypt = "$s0$c0801$OnXRutmFsj1Kb5XN4DG5Lg==$5xm3q0bRQSf72BPOOfq4T5AW/FMZ0ta1350e8Ou18A8=";
	@Test
	public void testEncrypt() {
		//System.setProperty("com.lambdaworks.jni.loader", "nil");
		String out = SCryptUtil.scrypt("test", 4096, 8, 1);
		
	
		Assert.assertTrue(SCryptUtil.check("test", out));
	}
	
	@Test
	public void testDecrypt() {
		Assert.assertTrue(SCryptUtil.check("test", crypt));
	}
}
