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
package io.macgyver.core.config;

import io.macgyver.test.MacGyverIntegrationTest;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.google.common.collect.Lists;

public class ConfigTest extends MacGyverIntegrationTest {

	@Test
	public void testCoreBeanNameStability() {
		Assertions.assertThat(applicationContext.getBean("macAdminController")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macCoreApiController")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macHomeController")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macLoginController")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macSearchController")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macSnapShareController")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macContextRefreshApplicationListener")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macAsyncHttpClient")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macSpringBeanPostProcessor")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macKernel")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macStartup")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macBindingSupplierManager")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macCoreBindingSupplier")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macCrypto")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macAutowiredPostProcessor")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macServiceRegistry")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macPropertyPlaceholderConfigurer")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macHookScriptManager")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macBeanFactoryPostProcessor")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macCoreRevisionInfo")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macUserManager")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macGraphClient")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macExtensionResourceProvider")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macClusterManager")).isNotNull();


		Assertions.assertThat(applicationContext.getBean("macEventLogger")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macEventLogWriter")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macTaskStateManager")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macScheduledTaskManager")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macTaskController")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macEventPublisher")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macAccessDecisionVoterList")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macAccessDecisionManager")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macInternalGroupRoleTranslator")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macGrantedAuthoritiesTranslatorChain")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macGrantedAuthoritiesTranslatorScriptHook")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macInternalAuthenticationProvider")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macTokenController")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macTokenAuthenticationProvider")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macTestBeanFactory")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macWeb")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macHandlerMappingPostProcessor")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macWebConfig")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macAccessLogCustomizer")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macNeo4jProxyServlet")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macNeo4jProxyServletRegistrationBean")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macContextFilter")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macFilterRegistration")).isNotNull();
		Assertions.assertThat(applicationContext.getBean("macUIContextManager")).isNotNull();
		
		
		// this generates the test assertions above
		boolean writeTests = false;
		if (writeTests) {
			Lists.newArrayList(applicationContext.getBeanDefinitionNames()).forEach(name -> {
				if (name.startsWith("mac")) {
					System.out.println("Assertions.assertThat(applicationContext.getBean(\"" + name + "\")).isNotNull();");
				}
			});
		}

	}
}
