package io.macgyver.core.service;

import java.util.Optional;

public interface ProxyConfig {

	public ProxyConfigManager getProxyConfigManager();
	String getProtocol();
	String getHost();
	int getPort();
	
	Optional<String> getUsername();
	Optional<String> getPassword();
	
}
