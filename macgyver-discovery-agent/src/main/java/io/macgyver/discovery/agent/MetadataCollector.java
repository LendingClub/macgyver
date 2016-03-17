package io.macgyver.discovery.agent;

import java.util.Properties;

public interface MetadataCollector {

	
	public static class MetadataProperties {
		Properties properties = new Properties();	
		public Properties getProperties() {
			return properties;
		}
		public MetadataProperties env(String val) {
			properties.put("env", val);
			return this;
		}
		
		public MetadataProperties appId(String val) {
			properties.put("appId", val);
			return this;
		}
		public MetadataProperties host(String host) {
			properties.put("host", host);
			return this;
		}
		public MetadataProperties version(String v) {
			properties.put("version", v);
			return this;
		}
		public MetadataProperties set(String key, String val) {
			properties.put(key, val);
			return this;
		}
		public MetadataProperties put(String key, String val) {
			set(key,val);
			return this;
		}
	}
	public void collect(MetadataProperties p);
	
}
