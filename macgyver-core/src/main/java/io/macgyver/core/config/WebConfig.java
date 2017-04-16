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
package io.macgyver.core.config;

import java.io.File;
import java.util.Map;

import org.apache.catalina.valves.AccessLogValve;
import org.apache.catalina.valves.RemoteIpValve;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.macgyver.core.Bootstrap;
import io.macgyver.core.web.MacGyverContextFilter;
import io.macgyver.core.web.UIContextManager;
import io.macgyver.core.web.mvc.CoreApiController;
import io.macgyver.core.web.mvc.HomeController;
import io.macgyver.core.web.mvc.MacGyverWeb;
import io.macgyver.core.web.neo4j.Neo4jProxyServlet;

@Configuration
@ComponentScan(basePackageClasses = { HomeController.class })
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
@EnableGlobalMethodSecurity(securedEnabled = true, proxyTargetClass = true, prePostEnabled = true)
public class WebConfig implements EnvironmentAware {

	Logger logger = LoggerFactory.getLogger(WebConfig.class);

	@Autowired
	private final org.springframework.core.io.ResourceLoader resourceLoader = new DefaultResourceLoader();

	@Autowired
	private Environment environment;

	@Value(value = "${MACGYVER_ACCESS_LOG_ENABLED:true}")
	boolean accessLogEnabled = true;

	@Value("${MACGYVER_SUPPRESS_ACCESS_LOG_ATTRIBUTE:MACGYVER_SUPPRESS_ACCESS_LOG}")
	String conditionalLoggingAttribute = "MACGYVER_SUPPRESS_ACCESS_LOG";
	
	@Override
	public void setEnvironment(Environment environment) {

	}

	@Bean
	public CoreApiController macCoreApiController() {
		return new CoreApiController();
	}

	@Bean
	public BeanPostProcessor macHandlerMappingPostProcessor() {
		return new BeanPostProcessor() {

			@Override
			public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
				if (bean instanceof RequestMappingHandlerMapping && "requestMappingHandlerMapping".equals(beanName)) {
					RequestMappingHandlerMapping m = ((RequestMappingHandlerMapping) bean);

				}

				return bean;
			}

			@Override
			public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
				return bean;
			}
		};
	}

	@Bean
	public MacGyverWeb macWebConfig() {
		return new MacGyverWeb();
	}

	@Bean
	public EmbeddedServletContainerCustomizer macAccessLogCustomizer() {

		EmbeddedServletContainerCustomizer customizer = new EmbeddedServletContainerCustomizer() {

			@Override
			public void customize(ConfigurableEmbeddedServletContainer container) {

				if (container instanceof TomcatEmbeddedServletContainerFactory) {
					if (accessLogEnabled) {
						File dir = Bootstrap.getInstance().getLogDir();
						try {
							dir.mkdirs();
						} catch (Exception ignore) {
						}
						if (dir.exists() && dir.isDirectory()) {
							logger.info("configuring access logs to be logged to: {}", dir);
							TomcatEmbeddedServletContainerFactory factory = (TomcatEmbeddedServletContainerFactory) container;

							RemoteIpValve remoteIpValve = new RemoteIpValve();
							remoteIpValve.setTrustedProxies(
									"10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|192\\.168\\.\\d{1,3}\\.\\d{1,3}|169\\.254\\.\\d{1,3}\\.\\d{1,3}|127\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|::1|0:0:0:0:0:0:0:1");
							remoteIpValve.setProtocolHeader("X-Forwarded-Proto");
							factory.addContextValves(remoteIpValve);

							AccessLogValve accessLogValve = new AccessLogValve();
							accessLogValve.setEnabled(accessLogEnabled);
							accessLogValve.setBuffered(false);
							accessLogValve.setCheckExists(true);
							accessLogValve.setDirectory(dir.getAbsolutePath());
							accessLogValve.setRotatable(true);
							accessLogValve.setRequestAttributesEnabled(true);
							accessLogValve.setPattern("%h %l %u %t \"%r\" %s %b \"%{Referer}i\" \"%{User-Agent}i\"");
							accessLogValve.setSuffix(".log");
							accessLogValve.setPrefix("macgyver_access");
							if (!Strings.isNullOrEmpty(conditionalLoggingAttribute)) {
								logger.info("configuring conditional access logging using attribute: {}",conditionalLoggingAttribute);
								accessLogValve.setConditionUnless(conditionalLoggingAttribute);
							}

							factory.addContextValves(accessLogValve);
						} else {
							logger.warn("cannot configure access log -- log directory does not exist: {}", dir);
						}
					} else {
						logger.error("WARNING! this customizer does not support your configured container");
					}
				}
				else {
					logger.info("core macgyver access log is disabled");
				}
			}
		};
		return customizer;
	}

	@Bean
	public Neo4jProxyServlet macNeo4jProxyServlet() {

		return new Neo4jProxyServlet();
	}

	@Bean
	public ServletRegistrationBean macNeo4jProxyServletRegistrationBean() {
		ServletRegistrationBean b = new ServletRegistrationBean();
		b.setUrlMappings(Lists.newArrayList("/browser/*", "/browser", "/db/*"));
		b.setServlet(macNeo4jProxyServlet());
		Map<String, String> x = Maps.newHashMap();

		b.setInitParameters(x);
		b.setName("Neo4jProxyServlet");
		return b;
	}

	@Bean
	MacGyverContextFilter macContextFilter() {
		return new MacGyverContextFilter();
	}

	@Bean
	FilterRegistrationBean macFilterRegistration() {
		FilterRegistrationBean b = new FilterRegistrationBean();
		b.setFilter(macContextFilter());
		b.setName("macContextFilter");
		return b;
	}

	@Bean
	UIContextManager macUIContextManager() {

		return new UIContextManager();
	}

}
