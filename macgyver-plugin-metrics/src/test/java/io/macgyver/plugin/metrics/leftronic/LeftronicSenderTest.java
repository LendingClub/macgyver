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
package io.macgyver.plugin.metrics.leftronic;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.macgyver.plugin.metrics.leftronic.LeftronicSender;

public class LeftronicSenderTest {

	LeftronicSender sender = new LeftronicSender(null);

	@Test
	public void testPayloadFormat() {

		LeftronicSender sender = new LeftronicSender(null);
		sender.setApiKey("secret");
		ObjectNode n = sender.formatPayloadForGauge("test", 1234);

		Assert.assertNotNull(n);
		Assert.assertEquals("test", n.get("streamName").asText());
		Assert.assertEquals(1234, n.get("point").asInt());
		Assert.assertEquals("secret", n.get("accessKey").asText());

	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullStreamName() {
		ObjectNode n = sender.formatPayloadForGauge(null, 11);
	}
	@Test(expected = IllegalArgumentException.class)
	public void testNullValue() {
		ObjectNode n = sender.formatPayloadForGauge("dummy",null);
	}
	@Test(expected = IllegalArgumentException.class)
	public void testEmptyStreamName() {
		ObjectNode n = sender.formatPayloadForGauge("", 11);
	}
}
