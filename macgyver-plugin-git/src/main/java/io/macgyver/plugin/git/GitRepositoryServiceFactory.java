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
package io.macgyver.plugin.git;

import io.macgyver.core.script.ExtensionResourceProvider;
import io.macgyver.core.service.ServiceDefinition;
import io.macgyver.core.service.ServiceFactory;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;

public class GitRepositoryServiceFactory extends ServiceFactory<GitRepository>{

	@Autowired
	ExtensionResourceProvider provider;
	
	public GitRepositoryServiceFactory() {
		super("GitRepository");
		
	}

	@Override
	public void doConfigureDefinition(ServiceDefinition def) {
		super.doConfigureDefinition(def);
		logger.info("Configuring: "+def);
	
		Properties p = def.getProperties();
		boolean isResourceProvider = Boolean.parseBoolean(p.getProperty("resourceProvider", "false"));
		if (isResourceProvider) {
			def.setLazyInit(false);

		}
		
	}

	@Override
	protected GitRepository doCreateInstance(ServiceDefinition def) {
		
		GitRepository repo = new GitRepository();
		Properties p = def.getProperties();
		repo.setUrl(p.getProperty("url"));
		repo.setUsername(p.getProperty("username"));
		repo.setPassword(p.getProperty("password"));
		
		if (Boolean.parseBoolean(p.getProperty("resourceProvider","false"))) {
			logger.info("using {} as macgyver extension for {}",def,provider);
			
			GitResourceProvider rp = new GitResourceProvider(repo);
			int fetchInterval = Integer.parseInt(p.getProperty("resourceProviderFetchInterval","300"));
			String ref = p.getProperty("resourceProviderGitRef","refs/heads/master");
			rp.setGitRef(ref);
			rp.setFetchIntervalSecs(fetchInterval);
			provider.replaceResourceProvider(rp);
	
		}
		
		
		return repo;
	}





}
