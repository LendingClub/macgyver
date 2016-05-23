package io.macgyver.core.service.config;

import java.io.File;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.macgyver.core.Bootstrap;
import io.macgyver.core.Kernel;

public class HJsonConfigLoader extends ConfigLoader {

	Logger logger = LoggerFactory.getLogger(HJsonConfigLoader.class);
	@Override
	public void applyConfig(Map<String, String> m) {
		
		File servicesJson = Bootstrap.getInstance().resolveConfig("services.hjson");
		
		if (servicesJson.exists()) {
			
		}
		
		servicesJson = Bootstrap.getInstance().resolveConfig("services.json");
		
		if (servicesJson.exists()) {
			
		}


	}

}
