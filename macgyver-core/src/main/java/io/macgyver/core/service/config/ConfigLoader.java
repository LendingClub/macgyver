package io.macgyver.core.service.config;

import java.util.Map;
import java.util.function.Function;

import com.beust.jcommander.internal.Maps;

public abstract class ConfigLoader implements Function<Map<String,String>, Map<String,String>>{

	@Override
	public final Map<String, String> apply(Map<String, String> t) {
		Map<String,String> m = Maps.newHashMap();
		m.putAll(t);
		applyConfig(t);
		return t;
	}
	
	public abstract void applyConfig(Map<String,String> m);

}
