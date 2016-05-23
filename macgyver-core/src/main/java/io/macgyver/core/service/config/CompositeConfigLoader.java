package io.macgyver.core.service.config;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

import io.macgyver.core.crypto.Crypto;

public class CompositeConfigLoader extends ConfigLoader {

	Logger logger = LoggerFactory.getLogger(CompositeConfigLoader.class);
	List<Function<Map<String,String>, Map<String,String>>> loaders = Lists.newCopyOnWriteArrayList();
	
	@Autowired
	Crypto crypto;
	
	public void addLoader(Function<Map<String,String>, Map<String,String>> f) {
		loaders.add(f);
	}
	public void addLoader(ConfigLoader cl) {
		loaders.add(cl);
	}


	@Override
	public void applyConfig(Map<String, String> m) {
		
		Map<String,String> data = m;
		for (Function<Map<String,String>,Map<String,String>> f: loaders) {
			
			logger.info("applying {}",f);
			data = f.apply(data);
			
			
		}
		
		logger.info("decrypting properties...");
		Properties p = new Properties();
		p.putAll(m);
		p = crypto.decryptProperties(p);
		
		p.entrySet().forEach(it -> {
			m.put(it.getKey().toString(), it.getValue().toString());
		});
		logger.info("property decryption complete.");
	}
	
	
}
