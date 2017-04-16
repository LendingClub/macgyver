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

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class RequestBuilderTest {

	@Test
	public void testIt() {
		A10ClientImpl c = Mockito.mock(A10ClientImpl.class);

		RequestBuilder b = new RequestBuilder(c, "test");

		Assertions.assertThat(b.getParams()).containsEntry("method", "test")
				.hasSize(1);

		Assertions.assertThat(b.method("foo").getParams())
				.containsEntry("method", "foo").hasSize(1);

		Assertions.assertThat(b.param("a", "1").getParams())
				.containsEntry("method", "foo").containsEntry("a", "1")
				.hasSize(2);
		Assertions.assertThat(b.param("a", "2").getParams())
				.containsEntry("method", "foo").containsEntry("a", "2")
				.hasSize(2);

	}

	@Test
	public void testFormat() {
		A10ClientImpl c = Mockito.mock(A10ClientImpl.class);

		RequestBuilder b = new RequestBuilder(c, "test");

		Assertions.assertThat(b.getParams()).doesNotContainKey("format");

		assertThat(b.withXmlRequest().getParams()).containsEntry("format", "xml");
		assertThat(b.withJsonRequest().getParams()).containsEntry("format", "json");

	}

	@Test
	public void testImmuableParams() {
		A10ClientImpl c = Mockito.mock(A10ClientImpl.class);

		RequestBuilder b = new RequestBuilder(c, "test");

		try {
			b.getParams().put("foo", "bar");
			Assert.fail();
		} catch (UnsupportedOperationException e) {
			
		}

	}
}
