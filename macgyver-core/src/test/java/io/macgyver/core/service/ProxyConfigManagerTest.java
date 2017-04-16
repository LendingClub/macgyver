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
package io.macgyver.core.service;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.macgyver.core.service.ProxyConfigManager.ProxyConfig;
import io.macgyver.test.MacGyverIntegrationTest;

public class ProxyConfigManagerTest extends MacGyverIntegrationTest {

	@Autowired
	ProxyConfigManager proxyConfigManager;
	
	@Test
	public void testIt() {
		Assertions.assertThat(proxyConfigManager).isNotNull();
		
		Assertions.assertThat(proxyConfigManager.getDefaultProxyConfig().isPresent()).isFalse();
		
		
		ProxyConfig cfg = proxyConfigManager.getProxyConfig("test0").get();
		Assertions.assertThat(cfg.getProtocol()).isEqualTo("http");
		Assertions.assertThat(cfg.getHost()).isEqualTo("foo");
		Assertions.assertThat(cfg.getPort()).isEqualTo(8000);
		Assertions.assertThat(cfg.getUsername().get()).isEqualTo("scott");
		Assertions.assertThat(cfg.getPassword().get()).isEqualTo("tiger");
		
		
		Assertions.assertThat(proxyConfigManager.getProxyConfig("").isPresent()).isFalse();
		Assertions.assertThat(proxyConfigManager.getProxyConfig(null).isPresent()).isFalse();
		Assertions.assertThat(proxyConfigManager.getProxyConfig("   ").isPresent()).isFalse();
		
		
		cfg = proxyConfigManager.getProxyConfig("test1").get();
		
		Assertions.assertThat(cfg.getPassword().isPresent()).isFalse();
		Assertions.assertThat(cfg.getUsername().isPresent()).isFalse();
	}
	
	@Test
	public void testNotFound() {
		Assertions.assertThat(proxyConfigManager.getProxyConfig("notfound").isPresent()).isFalse();
	}
}
