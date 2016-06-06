package io.macgyver.core.service.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import com.google.common.collect.Maps;

import io.macgyver.core.Kernel;

public class SpringConfigLoader extends ConfigLoader {

	static Logger logger = LoggerFactory.getLogger(SpringConfigLoader.class);

	@Override
	public void applyConfig(Map<String, String> m) {

		Environment env = Kernel.getApplicationContext().getEnvironment();
		if (env instanceof ConfigurableEnvironment) {
			getAllProperties((ConfigurableEnvironment) env);
		}

	}

	public static Map<String, String> getAllProperties(ConfigurableEnvironment aEnv) {
		Map<String, String> result = new HashMap<>();
		aEnv.getPropertySources().forEach(ps -> {
			result.putAll(getAllProperties(ps));
		});

		return result;
	}

	public static Map<String, String> getAllProperties(PropertySource<?> aPropSource) {
		Map<String, String> result = Maps.newHashMap();

		if (aPropSource instanceof CompositePropertySource) {
			CompositePropertySource cps = (CompositePropertySource) aPropSource;
			cps.getPropertySources().forEach(ps -> {
				result.putAll(getAllProperties(ps));
			});

			return result;
		}

		if (aPropSource instanceof EnumerablePropertySource<?>) {
			EnumerablePropertySource<?> ps = (EnumerablePropertySource<?>) aPropSource;
			Arrays.asList(ps.getPropertyNames()).forEach(key -> {
				String val = Objects.toString(ps.getProperty(key),null);
				result.put(key, val);
			});
			return result;
		}

		// note: Most descendants of PropertySource are
		// EnumerablePropertySource. There are some
		// few others like JndiPropertySource or StubPropertySource
		logger.warn("Given PropertySource is instanceof " + aPropSource.getClass().getName()
				+ " and cannot be iterated");

		return result;

	}
}
