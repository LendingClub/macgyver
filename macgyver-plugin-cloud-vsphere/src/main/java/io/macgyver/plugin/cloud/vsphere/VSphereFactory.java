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

import io.macgyver.core.Kernel;
import io.macgyver.core.MacGyverException;
import io.macgyver.core.service.BasicServiceFactory;
import io.macgyver.core.service.ServiceDefinition;
import io.macgyver.core.service.ServiceRegistry;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.lendingclub.mercator.core.Projector;
import org.lendingclub.mercator.vmware.VMWareScanner;
import org.lendingclub.mercator.vmware.VMWareScannerBuilder;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.thoughtworks.proxy.factory.CglibProxyFactory;
import com.thoughtworks.proxy.toys.delegate.DelegationMode;
import com.thoughtworks.proxy.toys.hotswap.HotSwapping;
import com.vmware.vim25.mo.ServiceInstance;

@Component
public class VSphereFactory extends BasicServiceFactory<ServiceInstance> {

	
	public VSphereFactory() {
		super("vsphere");

	}


	@Override
	protected Object doCreateInstance(ServiceDefinition def) {
	
		VMWareScanner scanner = Kernel.getApplicationContext().getBean(Projector.class).createBuilder(VMWareScannerBuilder.class)
		.withUrl(def.getProperties().getProperty("url"))
		.withUsername(def.getProperty("username"))
		.withPassword(def.getProperty("password"))
		.build();
		
		return scanner.getServiceInstance();
		
		
	}


	@Override
	protected void doCreateCollaboratorInstances(ServiceRegistry registry, ServiceDefinition def,
			Object primaryBean) {
		
		VMWareScanner scanner = Kernel.getApplicationContext().getBean(Projector.class).createBuilder(VMWareScannerBuilder.class)
				.withUrl(def.getProperties().getProperty("url"))
				.withUsername(def.getProperty("username"))
				.withPassword(def.getProperty("password"))
				.build();
		registry.registerCollaborator(def.getPrimaryName()+"Scanner", scanner);
	}


	@Override
	public void doCreateCollaboratorDefinitions(Set<ServiceDefinition> defSet, ServiceDefinition def) {
		ServiceDefinition templateDef = new ServiceDefinition(def.getName()
				+ "Scanner", def.getName(), def.getProperties(), this);
		defSet.add(templateDef);
	}


}
