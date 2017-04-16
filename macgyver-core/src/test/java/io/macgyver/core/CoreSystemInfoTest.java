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

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.rapidoid.u.U;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Supplier;
import com.google.common.collect.Maps;

import io.macgyver.test.MacGyverIntegrationTest;

public class CoreSystemInfoTest extends MacGyverIntegrationTest {

	@Autowired
	CoreSystemInfo coreSystemInfo;
	
	@Test
	public void testCoreSystemInfo() {
	
		coreSystemInfo.setExtraMetadataSupplier(new Supplier<Map<String,String>>() {
			
			@Override
			public Map<String, String> get() {
				return U.map("foo", "bar");
				
			}
		});
		Assertions.assertThat(coreSystemInfo.getData()).containsEntry("foo", "bar");
	}
}
