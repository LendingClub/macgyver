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

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.thoughtworks.proxy.factory.CglibProxyFactory;
import com.thoughtworks.proxy.toys.hotswap.HotSwapping;
import com.thoughtworks.proxy.toys.hotswap.Swappable;

public class HotSwapProxyTest {

	public static class Foo {
		
		String val;
		
		public Foo(String v) {
			this.val = v;
		}
		
		public String getBar() {
			return val;
		}
	}
	@Test
	public void testIt() {
		
		Foo f1 = new Foo("1");
		
		Foo f2 = new Foo("2");
		
		Foo fx = HotSwapping.proxy(Foo.class).with(null).build(new CglibProxyFactory());
		
		Swappable.class.cast(fx).hotswap(f1);
		Assertions.assertThat(fx.getBar()).isEqualTo("1");
		Swappable.class.cast(fx).hotswap(f2);
		Assertions.assertThat(fx.getBar()).isEqualTo("2");
	}
}
