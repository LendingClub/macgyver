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
package io.macgyver.core.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.ProxySelector;
import java.net.Socket;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.CacheLoader.InvalidCacheLoadException;
import com.google.common.collect.Maps;

import io.macgyver.core.service.config.CompositeConfigLoader;
import io.macgyver.okrest3.OkRestClient;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class ProxyConfigManager {

	public static final int DEFAULT_PROXY_PORT = 8080;
	static Logger logger = LoggerFactory.getLogger(ProxyConfigManager.class);

	public interface ProxyConfig {

		public ProxyConfigManager getProxyConfigManager();
		String getProtocol();
		String getHost();
		int getPort();
		
		Optional<String> getUsername();
		Optional<String> getPassword();
		
		public OkHttpClient.Builder apply(OkHttpClient.Builder builder);
		public OkRestClient.Builder apply(OkRestClient.Builder builder);
		public RequestConfig.Builder apply(RequestConfig.Builder builder);
		
	}
	@Autowired
	CompositeConfigLoader configLoader;

	volatile LoadingCache<String, ProxyConfig> cache = CacheBuilder.newBuilder()
			.expireAfterWrite(5 * 365, TimeUnit.DAYS).build(new ProxyCacheLoader());

	class ProxyConfigImpl implements ProxyConfig {

		String protocol = "http";
		String host;
		int port = 8080;
		String username;
		String password;

		public ProxyConfigManager getProxyConfigManager() {
			return ProxyConfigManager.this;
		}
		public String getProtocol() {
			return protocol;
		}

		@Override
		public String getHost() {
			return host;
		}

		@Override
		public int getPort() {
			return port;
		}

		@Override
		public Optional<String> getUsername() {
			return Optional.ofNullable(username);
		}

		@Override
		public Optional<String> getPassword() {
			return Optional.ofNullable(password);
		}

		
		public String toString() {
			return MoreObjects.toStringHelper(this).add("protocol", protocol).add("host", host).add("port", port)
					.add("username", username).add("password", getPassword().isPresent() ? "******" : null).toString();
		}
		@Override
		public OkHttpClient.Builder apply(Builder builder) {
			return getProxyConfigManager().apply(builder, this);
			
		}
		@Override
		public io.macgyver.okrest3.OkRestClient.Builder apply(io.macgyver.okrest3.OkRestClient.Builder builder) {
			return getProxyConfigManager().apply(builder, this);
			
		}
		@Override
		public org.apache.http.client.config.RequestConfig.Builder apply(org.apache.http.client.config.RequestConfig.Builder builder) {
			return getProxyConfigManager().apply(builder, this);
			
		}
	}

	class ProxyCacheLoader extends CacheLoader<String, ProxyConfig> {

		String key(String name, String key) {
			return String.format("proxy.%s.%s", name, key);
		}

		@Override
		public ProxyConfig load(String name) throws Exception {

			Map<String, String> data = Maps.newHashMap();
			ProxyConfigImpl pci = new ProxyConfigImpl();

			configLoader.applyConfig(data);

			pci.protocol = data.get(key(name, "protocol"));
			if (Strings.isNullOrEmpty(pci.protocol)) {
				pci.protocol = "http";
			} else if (pci.protocol.equals("http")) {
				pci.protocol = "http";
			} else {
				throw new IllegalArgumentException("proxy protocol not supported: " + pci.protocol);
			}
			pci.host = data.get(key(name, "host"));
			pci.port = DEFAULT_PROXY_PORT;
			if (Strings.isNullOrEmpty(pci.host)) {
				return null;
			}
			String portString = data.get(key(name, "port"));
			if (Strings.isNullOrEmpty(portString)) {
				pci.port = DEFAULT_PROXY_PORT;
			} else {
				try {
					pci.port = Integer.parseInt(portString);
				} catch (NumberFormatException e) {
					logger.error("could not parse {}: '{}'", key(name, "port"), portString);
					return null;
				}

			}
			pci.username = data.get(key(name, "username"));
			pci.password = data.get(key(name, "password"));
			return pci;
		}

	}

	/**
	 * This provides a way for implementors to supply their own mechanism for
	 * proxy config settings.
	 * 
	 * @param cfg
	 */

	public void setProxyConfigLoader(CacheLoader<String, ProxyConfig> cfg) {
		this.cache = CacheBuilder.newBuilder().expireAfterWrite(5 * 365, TimeUnit.DAYS).build(cfg);
	}

	public Optional<ProxyConfig> getDefaultProxyConfig() {
		return getProxyConfig("default");
	}

	public void clearCache() {
		cache.invalidateAll();
	}

	public Optional<ProxyConfig> getProxyConfig(String name) {
		if (name == null) {
			return Optional.empty();
		}
		try {
			return Optional.ofNullable(cache.get(name));
		} catch (InvalidCacheLoadException e) {
			logger.warn("proxy config for '{}' not found", name);
			return Optional.empty();
		} catch (ExecutionException e) {
			logger.error("problem loading proxy config", e);
			return Optional.empty();
		}
	}

	public <T extends HttpRequestBase> T apply(T request, String proxyConfigName) {
		Optional<ProxyConfig> cfg = getProxyConfig(proxyConfigName);
		if (cfg.isPresent()) {
			RequestConfig.Builder builder = RequestConfig.custom();

			builder = apply(builder, cfg.get());

			request.setConfig(builder.build());
			return request;
		}
		return request;

	}
	public OkRestClient.Builder apply(OkRestClient.Builder builder, ProxyConfig proxyConfig) {
		builder = builder.withOkHttpClientConfig(cc->{
			proxyConfig.getProxyConfigManager().apply(cc, proxyConfig);
		});
		return builder;
	}
	public OkRestClient.Builder apply(OkRestClient.Builder builder, String proxyConfigName) {
		Optional<ProxyConfig> cfg = getProxyConfig(proxyConfigName);
		if (cfg.isPresent()) {
			return apply(builder, cfg.get());
		}

		return builder;
	}
	public RequestConfig.Builder apply(RequestConfig.Builder builder, String proxyConfigName)  {
		Optional<ProxyConfig> cfg = getProxyConfig(proxyConfigName);
		if (cfg.isPresent()) {
			return apply(builder, cfg.get());
		}

		return builder;
	}

	public RequestConfig.Builder apply(RequestConfig.Builder requestConfig, ProxyConfig proxyConfig) {

		if (proxyConfig != null) {
			if (proxyConfig.getProtocol().equals("http")) {
				HttpHost host = new HttpHost(proxyConfig.getHost(), proxyConfig.getPort());
				requestConfig.setProxy(host);

			} else {
				throw new IllegalArgumentException("proxy protocol not supported: " + proxyConfig.getProtocol());
			}
		}
		return requestConfig;
	}

	public OkHttpClient.Builder apply(OkHttpClient.Builder builder, String proxyConfigName)  {
		Optional<ProxyConfig> cfg = getProxyConfig(proxyConfigName);
		if (cfg.isPresent()) {
			return apply(builder, cfg.get());
		}

		return builder;
	}

	public OkHttpClient.Builder apply(OkHttpClient.Builder builder, ProxyConfig config)  {

		if (config != null) {
			if (config.getProtocol().equals("http")) {
				Proxy proxy = new Proxy(Type.HTTP, new InetSocketAddress(config.getHost(), config.getPort()));
				builder = builder.proxy(proxy);

				if (config.getUsername().isPresent() || config.getPassword().isPresent()) {
					Authenticator proxyAuthenticator = new Authenticator() {
						@Override
						public Request authenticate(Route route, Response response) throws IOException {
							String credential = Credentials.basic(config.getUsername().orElse(""),
									config.getPassword().orElse(""));
							return response.request().newBuilder().header("Proxy-Authorization", credential).build();
						}
					};
					builder = builder.proxyAuthenticator(proxyAuthenticator);
				}
			} else {
				throw new IllegalArgumentException("proxy protocol not supported: " + config.getProtocol());
			}
		}
		return builder;
	}
}
