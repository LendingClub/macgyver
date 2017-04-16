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
package io.macgyver.core.web.neo4j;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class Neo4jProxyServletTest {

	Neo4jProxyServlet proxy = new Neo4jProxyServlet();


	@Test
	public void testCheckReadOnly() {
		Assertions.assertThat(proxy.isCypherReadOnly(" crEAte x")).isFalse();
		Assertions.assertThat(proxy.isCypherReadOnly("CREATE foo")).isFalse();
		Assertions.assertThat(proxy.isCypherReadOnly("blah CREATE blah")).isFalse();

		Assertions.assertThat(proxy.isCypherReadOnly("match (m) set m.name='foo' return m")).isFalse();
		Assertions.assertThat(proxy.isCypherReadOnly(" \rcrEAte\n   x")).isFalse();
		Assertions.assertThat(proxy.isCypherReadOnly(" createx x")).isTrue();
		Assertions.assertThat(proxy.isCypherReadOnly(" is_create ")).isTrue();
		Assertions.assertThat(proxy.isCypherReadOnly(" create_date ")).isTrue();
	}
}
