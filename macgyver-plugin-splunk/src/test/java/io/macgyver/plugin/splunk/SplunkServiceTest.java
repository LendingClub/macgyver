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
package io.macgyver.plugin.splunk;

import io.macgyver.core.service.ServiceDefinition;
import io.macgyver.core.service.ServiceRegistry;
import io.macgyver.test.MacGyverIntegrationTest;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.splunk.HttpService;
import com.splunk.SSLSecurityProtocol;
import com.splunk.ServiceArgs;

public class SplunkServiceTest  {



	@Test
	public void testSecurityProtocol() throws IOException {




		Assertions.assertThat(SSLSecurityProtocol.valueOf("TLSv1_2")).isSameAs(SSLSecurityProtocol.TLSv1_2);
		Assertions.assertThat(SSLSecurityProtocol.valueOf("TLSv1_1")).isSameAs(SSLSecurityProtocol.TLSv1_1);
		Assertions.assertThat(SSLSecurityProtocol.valueOf("TLSv1")).isSameAs(SSLSecurityProtocol.TLSv1);

	}







}
