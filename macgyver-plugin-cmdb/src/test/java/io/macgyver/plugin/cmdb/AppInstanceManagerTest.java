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
package io.macgyver.plugin.cmdb;

import io.macgyver.test.MacGyverIntegrationTest;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AppInstanceManagerTest extends MacGyverIntegrationTest {
	@Autowired
	AppInstanceManager manager;

	SecureRandom secureRandom;

	public AppInstanceManagerTest() throws NoSuchAlgorithmException {
		super();

		secureRandom = SecureRandom.getInstance("sha1prng");
	}

	@Test
	public void testUnknownHostname() {
		String host = "UNKNOWN";
		String appId = "myapp";
		String groupId = "mygroup";

		ObjectNode node = new ObjectMapper().createObjectNode().put("host", host).put("appId", appId).put("groupId",
				groupId);

		ObjectNode v1 = manager.processCheckIn(node);

		Assertions.assertThat(v1.size()).isEqualTo(0);

	}

	@Test
	public void testLocalhostHostname() {
		String host = "localhost";
		String appId = "myapp";
		String groupId = "mygroup";

		ObjectNode node = new ObjectMapper().createObjectNode().put("host", host).put("appId", appId).put("groupId",
				groupId);

		ObjectNode v1 = manager.processCheckIn(node);

		Assertions.assertThat(v1.size()).isEqualTo(0);

	}

	ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testComputeSignature() {
		AppInstanceManager appInstanceManager = new AppInstanceManager();

		Assertions
				.assertThat(appInstanceManager
						.computeSignature(mapper.createObjectNode().put("foo", "bar").put("abc", "def")))
				.isEqualTo(appInstanceManager
						.computeSignature(mapper.createObjectNode().put("abc", "def").put("foo", "bar")));
		
		
		
		Assertions
		.assertThat(appInstanceManager
				.computeSignature(mapper.createObjectNode().put("foo", "bar").put("abc", "def")))
		.isEqualTo(appInstanceManager
				.computeSignature((ObjectNode)mapper.createObjectNode().put("abc", "def").put("foo", "bar").set("abcdef", mapper.createObjectNode())));

	}



	//
	// @Test
	// public void testMultiThead() throws IOException {
	// ThreadPoolExecutor x = null;
	// try {
	// int threadCount = 5;
	// long t0 = System.currentTimeMillis();
	// int iterationCount = 1000;
	// final int keySpace = 50;
	//
	// BlockingQueue<Runnable> q = new ArrayBlockingQueue<>(iterationCount);
	// for (int i = 0; i < iterationCount; i++) {
	// final Runnable r = new Runnable() {
	//
	// @Override
	// public void run() {
	//
	// String x = "junit_group_"
	// + (Math.abs(randomInt()) % keySpace);
	// ObjectNode v = manager.getOrCreateAppInstance(x,
	// x, x);
	// /*
	// * v.setProperty("someproperty_" + (new
	// * Random().nextInt() % keySpace), UUID.randomUUID()
	// * .toString());
	// */
	//
	// }
	//
	// };
	// q.add(r);
	// }
	//
	// x = new ThreadPoolExecutor(threadCount, threadCount, 5,
	// TimeUnit.SECONDS, q);
	//
	// x.prestartAllCoreThreads();
	//
	// while (!q.isEmpty()) {
	// try {
	// Thread.sleep(10L);
	// } catch (Exception e) {
	// }
	// }
	//
	// long t1 = System.currentTimeMillis();
	// long tdur = t1 - t0;
	// logger.info(
	// "processed {} getOrCreateAppInstance calls in {}ms ({}/sec) using {}
	// threads",
	// iterationCount, tdur,
	// ((double) iterationCount / tdur) * 1000, threadCount);
	// } finally {
	// if (x!=null) {
	// x.shutdown();
	// }
	// }
	// }

}
