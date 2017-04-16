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
package io.macgyver.plugin.elb.a10;

import io.macgyver.test.MacGyverIntegrationTest;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;

public class A10ClientIntegrationTest extends MacGyverIntegrationTest {

	static boolean hasConnectivity = true;
	static A10ClientImpl client;

	@Before
	public void setupClient() {
		Assume.assumeTrue("junit can communicate with A10", hasConnectivity);
		if (!hasConnectivity) {
			return;
		}
		if (client == null && hasConnectivity) {
			String url = getPrivateProperty("a10.url");
			String username = getPrivateProperty("a10.username");
			String password = getPrivateProperty("a10.password");

			Assume.assumeTrue(!Strings.isNullOrEmpty(url));
			Assume.assumeTrue(!Strings.isNullOrEmpty(username));
			Assume.assumeTrue(!Strings.isNullOrEmpty(password));
			client = new A10ClientImpl(url, username, password);
			client.setCertificateVerificationEnabled(false);

			try {
				client.authenticate();
			} catch (Exception e) {
				logger.warn("problem communicating with A10", e);
				hasConnectivity = false;
				Assume.assumeTrue("junit has connectivity with A10",
						hasConnectivity);
			}
		}
	}

	@Test
	public void testAuthFailure() {

		try {
			client.setPassword("test");
			client.authenticate();
			Assert.fail("authentication should have failed");

		} catch (A10RemoteException e) {
			// Maybe we want to test the code/message?
			// io.macgyver.elb.a10.A10RemoteException: 520486915: Admin password
			// error
		}
	}


	@Test
	public void testTokenCache() {
		
		String t1 = client.getAuthToken();
		String t2 = client.getAuthToken();

		Assert.assertEquals(t1, t2);

	}

	@Test
	public void testTokenCacheDisabled() throws InterruptedException {

		client.setTokenCacheDuration(0, TimeUnit.MICROSECONDS);
		String t1 = client.getAuthToken();
		Thread.sleep(100);
		String t2 = client.getAuthToken();

		Assert.assertNotEquals(t1, t2);

	}








}
