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
package io.macgyver.core.web;

import static io.macgyver.core.util.JsonNodes.pretty;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import io.macgyver.core.util.JsonNodes;
import io.macgyver.core.web.UIContext.MenuItem;

public class UIDescriptorTest {


	@Test
	public void testIt() {
		
		UIContext d = new UIContext();
		
		Assertions.assertThat(d.getModel().has("menu")).isTrue();
		Assertions.assertThat(d.getModel().path("menu").path("id").asText()).isEqualTo("root");
		
		
		
		System.out.println(d.getModel());
		
		io.macgyver.core.web.UIContext.MenuItem m = d.getOrCreateMenuItem("test");
		MenuItem m2 = d.getOrCreateMenuItem("test");
		
		Assertions.assertThat(m.getModel().path("id").asText()).isEqualTo("test");
		Assertions.assertThat(m.getModel().path("items"));
		Assertions.assertThat(m.getModel()).isSameAs(m2.getModel());
		d.getOrCreateMenuItem("test");
		d.getOrCreateMenuItem("test","a");
		d.getOrCreateMenuItem("test2").label("def");
		d.getOrCreateMenuItem("test3").label("abc");
		
		d.sort();
		Assertions.assertThat(d.getOrCreateMenuItem("test","a").getId()).isEqualTo("a");
		System.out.println(pretty(d.getModel()));
		
		
	}

}
