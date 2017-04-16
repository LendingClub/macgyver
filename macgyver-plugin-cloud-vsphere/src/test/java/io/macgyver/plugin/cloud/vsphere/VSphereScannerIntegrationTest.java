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
package io.macgyver.plugin.cloud.vsphere;

import java.net.URL;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.lendingclub.mercator.vmware.RefreshingServiceInstance;
import org.lendingclub.neorx.NeoRxClient;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Strings;
import com.thoughtworks.proxy.factory.CglibProxyFactory;
import com.thoughtworks.proxy.toys.delegate.DelegationMode;
import com.thoughtworks.proxy.toys.hotswap.HotSwapping;
import com.vmware.vim25.mo.ServiceInstance;

import io.macgyver.test.MacGyverIntegrationTest;
public class VSphereScannerIntegrationTest extends MacGyverIntegrationTest {

	
	String url;
	String user;
	String pass;
	ServiceInstance serviceInstance;
	
	@Autowired
	NeoRxClient neo4j;
	
	@Before
	public void setupCreds() {
		 url = getPrivateProperty("vcenter.url");
		 user = getPrivateProperty("vcenter.username");
		 pass = getPrivateProperty("vcenter.password");
		
		Assume.assumeFalse("url must be set",Strings.isNullOrEmpty(url));
		Assume.assumeFalse("username must be set",Strings.isNullOrEmpty(user));
		Assume.assumeFalse("password must be set",Strings.isNullOrEmpty(pass));
	
		
		try {
			serviceInstance = new RefreshingServiceInstance(new URL(url), user,pass,true);
			
		
			RefreshingServiceInstance cglibProxy  = HotSwapping.proxy(RefreshingServiceInstance.class).with(serviceInstance).mode(DelegationMode.DIRECT).build(new CglibProxyFactory());
			
			serviceInstance.getServerConnection().logout();
			RefreshingServiceInstance.refreshToken(cglibProxy);
			cglibProxy.currentTime();
			
			serviceInstance = cglibProxy;
		}
		catch (Exception e) {
			e.printStackTrace();
			Assume.assumeTrue(false);
		}
	}
	
	
	
	@Test
	public void testIt() {
		
	}
	

	
}
