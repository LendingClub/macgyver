package io.macgyver.plugin.config;

import io.macgyver.plugin.cmdb.AppInstanceManager;
import io.macgyver.plugin.cmdb.CmdbApiController;
import io.macgyver.plugin.cmdb.CmdbUIDecorator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
	public CmdbUIDecorator macCmdbUIManager() {
		return new CmdbUIDecorator();
	}
}
