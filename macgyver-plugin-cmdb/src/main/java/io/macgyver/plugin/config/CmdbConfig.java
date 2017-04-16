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
package io.macgyver.plugin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import io.macgyver.plugin.cmdb.AppInstanceController;
import io.macgyver.plugin.cmdb.AppInstanceManager;
import io.macgyver.plugin.cmdb.CmdbApiController;
import io.macgyver.plugin.cmdb.CmdbMenuDecorator;
import io.macgyver.plugin.cmdb.catalog.AppDefinitionController;
import io.macgyver.plugin.cmdb.catalog.AppDefinitionLoader;
import io.macgyver.plugin.cmdb.catalog.DatabaseDefinitionController;
import io.macgyver.plugin.cmdb.catalog.DatabaseDefinitionLoader;
import io.macgyver.plugin.cmdb.catalog.JobDefinitionController;
import io.macgyver.plugin.cmdb.catalog.JobDefinitionLoader;
import io.macgyver.plugin.cmdb.catalog.QueueDefinitionController;
import io.macgyver.plugin.cmdb.catalog.QueueDefinitionLoader;
import io.macgyver.plugin.cmdb.catalog.StreamDefinitionController;
import io.macgyver.plugin.cmdb.catalog.StreamDefinitionLoader;

@Configuration
public class CmdbConfig {


	@Bean
	public AppInstanceManager macAppInstanceManager() {
		
		return new AppInstanceManager();
	}
	
	@Bean
	public CmdbApiController macCmdbApiController() {
		return new CmdbApiController();
	}

	
	
	@Bean
	public AppInstanceController macAppInstanceController() {
		return new AppInstanceController();
	}
	@Bean
	public CmdbMenuDecorator macCmdbMenuDecorator() {
		return new CmdbMenuDecorator();
	}
	@Bean
	public JobDefinitionLoader macJobCatalogLoader() {
		return new JobDefinitionLoader();
	}
	@Bean
	public AppDefinitionLoader macServiceCatalogLoader() {
		return new AppDefinitionLoader();
	}
	
	@Bean
	public AppDefinitionController macServiceCatalogController() {
		return new AppDefinitionController();
	}
	
	@Bean
	public JobDefinitionController macJobCatalogController() {
		return new JobDefinitionController();
	}
	
	@Bean
	public DatabaseDefinitionLoader macDatabaseDefinitionLoader() {
		return new DatabaseDefinitionLoader();
	}
	
	@Bean
	public DatabaseDefinitionController macDatabaseDefinitionController() {
		return new DatabaseDefinitionController();
	}
	
	@Bean
	public QueueDefinitionController macQueueDefinitionController() {
		return new QueueDefinitionController();
	}
	@Bean
	public QueueDefinitionLoader macQueueDefinitionLoader() {
		return new QueueDefinitionLoader();
	}
	
	@Bean
	public StreamDefinitionController macStreamDefinitionController() {
		return new StreamDefinitionController();
	}
	@Bean
	public StreamDefinitionLoader macStreamDefinitionLoader() {
		return new StreamDefinitionLoader();
	}
	
	@Bean
	public io.macgyver.plugin.cmdb.AppEventApiController appEventApiController() {
		return new io.macgyver.plugin.cmdb.AppEventApiController();
	}

}
