/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.macgyver.core.config;

import static springfox.documentation.builders.PathSelectors.regex;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.lendingclub.mercator.core.Projector;
import org.lendingclub.neorx.NeoRxClient.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jmx.export.MBeanExporter;

import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Slf4jReporter.LoggingLevel;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.ning.http.client.AsyncHttpClient;

import io.macgyver.core.Bootstrap;
import io.macgyver.core.ContextRefreshApplicationListener;
import io.macgyver.core.CoreBindingSupplier;
import io.macgyver.core.CoreSystemInfo;
import io.macgyver.core.Kernel;
import io.macgyver.core.MacGyverBeanFactoryPostProcessor;
import io.macgyver.core.MacGyverBeanPostProcessor;
import io.macgyver.core.ScriptHookManager;
import io.macgyver.core.Startup;
import io.macgyver.core.auth.ApiTokenAuthenticationProvider;
import io.macgyver.core.auth.UserManager;
import io.macgyver.core.cli.CLIDownloadController;
import io.macgyver.core.cluster.ClusterManager;
import io.macgyver.core.crypto.Crypto;
import io.macgyver.core.event.EventLogger;
import io.macgyver.core.event.EventSystem;
import io.macgyver.core.event.MacGyverEventPublisher;
import io.macgyver.core.event.Neo4jEventLogWriter;
import io.macgyver.core.event.Slf4jEventWriter;
import io.macgyver.core.metrics.MacGyverMetricRegistry;
import io.macgyver.core.resource.provider.filesystem.FileSystemResourceProvider;
import io.macgyver.core.scheduler.LocalScheduler;
import io.macgyver.core.scheduler.MacGyverTaskCollector;
import io.macgyver.core.scheduler.ScheduledTaskManager;
import io.macgyver.core.scheduler.TaskController;
import io.macgyver.core.scheduler.TaskStateManager;
import io.macgyver.core.script.BindingSupplierManager;
import io.macgyver.core.script.ExtensionResourceProvider;
import io.macgyver.core.service.ProxyConfigManager;
import io.macgyver.core.service.ServiceRegistry;
import io.macgyver.core.service.config.CompositeConfigLoader;
import io.macgyver.core.service.config.HJsonConfigLoader;
import io.macgyver.core.service.config.Neo4jConfigLoader;
import io.macgyver.core.service.config.ServicesGroovyConfigLoader;
import io.macgyver.core.service.config.SpringConfigLoader;
import it.sauronsoftware.cron4j.Scheduler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.ApiKeyVehicle;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.UiConfiguration;

@Configuration
public class CoreConfig implements EnvironmentAware {

	@Autowired
	org.springframework.core.env.Environment env;

	static Logger logger = LoggerFactory.getLogger(CoreConfig.class);

	@Bean
	public ContextRefreshApplicationListener macContextRefreshApplicationListener() {
		return new ContextRefreshApplicationListener();
	}

	@Bean(name = "macAsyncHttpClient", destroyMethod = "close")
	public AsyncHttpClient macAsyncHttpClient() {
		return new AsyncHttpClient();
	}

	@Bean
	public MacGyverBeanPostProcessor macSpringBeanPostProcessor() {
		return new MacGyverBeanPostProcessor();
	}

	@Bean(name = "macKernel")
	public Kernel macKernel() {

		return new Kernel();
	}

	@Bean
	public Startup macStartup() {
		return new Startup();
	}

	@Bean
	public BindingSupplierManager macBindingSupplierManager() {
		return new BindingSupplierManager();
	}

	@Bean
	public CoreBindingSupplier macCoreBindingSupplier() {
		return new CoreBindingSupplier();
	}

	@Bean
	public Crypto macCrypto() {
		Crypto crypto = new Crypto();
		Crypto.instance = crypto;
		return crypto;
	}

	@Bean(name = "testOverride")
	public Properties testOverride() {
		Properties props = new Properties();
		props.put("x", "from coreconfig");
		return props;
	}

	@Bean
	public static AutowiredAnnotationBeanPostProcessor macAutowiredPostProcessor() {
		return new AutowiredAnnotationBeanPostProcessor();

	}

	@Bean
	public ServiceRegistry macServiceRegistry() {
		return new ServiceRegistry();
	}

	public static boolean isUnitTest() {

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		new RuntimeException().printStackTrace(pw);
		pw.close();
		return sw.toString().contains("at org.junit");

	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer macPropertyPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean
	public ScriptHookManager macHookScriptManager() {
		return new ScriptHookManager();
	}

	@Bean
	public static MacGyverBeanFactoryPostProcessor macBeanFactoryPostProcessor() {
		return new MacGyverBeanFactoryPostProcessor();
	}

	@Bean(name = "macCoreRevisionInfo")
	public CoreSystemInfo macCoreRevisionInfo() {
		return new CoreSystemInfo();
	}

	@Bean(name = "macUserManager")
	public UserManager macUserManager() {
		return new UserManager();
	}

	/*@Bean(name = "macGraphClient")
	public NeoRxClient macGraphClient() throws MalformedURLException {
		Preconditions.checkNotNull(env);
		String url = env.getProperty("neo4j.url");

		if (Strings.isNullOrEmpty(url)) {
			url = "http://localhost:7474";
		}
		logger.info("neo4j.url: {}", url);

		boolean validateCerts = false;

		return new NeoRxClientBuilder().withCertificateValidation(validateCerts)
				.withCredentials(env.getProperty("neo4j.username"), env.getProperty("neo4j.password")).build();

	}*/

	@Bean(name = "macGraphClient")
	public org.lendingclub.neorx.NeoRxClient macGraphClient() {
		Preconditions.checkNotNull(env);
		String url = env.getProperty("neo4j.bolt.url");

		if (Strings.isNullOrEmpty(url)) {
			url = "bolt://localhost:7687";
		}
		logger.info("neo4j.bolt.url: {}", url);

		Builder builder = new org.lendingclub.neorx.NeoRxClient.Builder();
		String username = env.getProperty("neo4j.username");
		String password = env.getProperty("neo4j.password");
		if (!(Strings.isNullOrEmpty(username) && Strings.isNullOrEmpty(password))) {
			builder = builder.withCredentials(username, password);
		}
		return builder.build();
	}

	@Bean(name = "macProjector")
	public Projector macProjector() throws MalformedURLException {
		return new Projector.Builder().withNeoRxClient(macGraphClient()).build();
	}

	@Bean
	public ExtensionResourceProvider macExtensionResourceProvider() {
		ExtensionResourceProvider loader = new ExtensionResourceProvider();

		FileSystemResourceProvider fsLoader = new FileSystemResourceProvider(Bootstrap.getInstance().getMacGyverHome());
		loader.addResourceLoader(fsLoader);

		return loader;
	}

	@Override
	public void setEnvironment(org.springframework.core.env.Environment environment) {
		this.env = environment;

	}

	@Bean
	public ClusterManager macClusterManager() {
		return new ClusterManager();
	}

	@Autowired(required = false)
	@Qualifier("mbeanExporter")
	MBeanExporter springMBeanExporter;

	@Bean
	public EventLogger macEventLogger() {
		return new EventLogger();
	}

	@Bean
	public Neo4jEventLogWriter macEventLogWriter() {
		return new Neo4jEventLogWriter();
	}

	@Bean
	public TaskStateManager macTaskStateManager() {
		return new TaskStateManager();
	}

	@Bean
	public ScheduledTaskManager macScheduledTaskManager() {
		return new ScheduledTaskManager();
	}

	@Bean
	public TaskController macTaskController() {
		return new TaskController();
	}

	@Bean
	public MacGyverEventPublisher macEventPublisher() {
		return new MacGyverEventPublisher();
	}

	@Bean
	public CLIDownloadController macCliDownloadController() {
		return new CLIDownloadController();
	}

	@Bean(name = "macMetricRegistry")
	public MacGyverMetricRegistry macMetricRegistry() {

		MacGyverMetricRegistry registry = new MacGyverMetricRegistry();
		SharedMetricRegistries.add("macMetricRegistry", registry);

		Slf4jReporter r = Slf4jReporter.forRegistry(registry).withLoggingLevel(LoggingLevel.DEBUG)
				.convertDurationsTo(TimeUnit.MILLISECONDS).convertRatesTo(TimeUnit.SECONDS)
				.outputTo(LoggerFactory.getLogger("io.macgyver.metrics")).build();

		r.start(60, TimeUnit.SECONDS);

		return registry;
	}

	@Bean
	public MacGyverTaskCollector macTaskCollector() {
		return new MacGyverTaskCollector();
	}

	@Bean
	public LocalScheduler macLocalScheduler() {
		return new LocalScheduler();
	}

	@Bean
	public Scheduler macCron4jScheduler() {
		return new Scheduler();
	}

	private SecurityContext securityContext() {
		return SecurityContext.builder().securityReferences(defaultAuth()).forPaths(PathSelectors.regex("/api.*"))
				.build();
	}

	List<SecurityReference> defaultAuth() {
		AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
		AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
		authorizationScopes[0] = authorizationScope;
		return Lists.newArrayList(new SecurityReference("mykey", authorizationScopes));
	}

	@Bean
	SecurityConfiguration security() {
		return new SecurityConfiguration("test-app-client-id", "test-app-client-secret", "test-app-realm", "test-app",
				"apiKey", ApiKeyVehicle.HEADER, ApiTokenAuthenticationProvider.API_KEY_HEADER_NAME,
				"," /* scope separator */);
	}

	@Bean
	public Docket springfoxDocket() {
		return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors.any())
				.paths(regex("/api/.*")).build().pathMapping("/")
				.securitySchemes(Lists.newArrayList(
						new ApiKey("apikey", ApiTokenAuthenticationProvider.API_KEY_HEADER_NAME, "header")))
				// .securityContexts(Lists.newArrayList(securityContext())) //
				// UNUSED
				.apiInfo(metadata());
	}

	@Bean
	public UiConfiguration springfoxUiConfig() {
		return UiConfiguration.DEFAULT;
	}

	private ApiInfo metadata() {
		return new ApiInfoBuilder().title("MacGyver API").description("API Documentation").version("1.0").build();
	}

	@Bean
	public Neo4jConfigLoader macNeo4jConfigLoader() {
		return new Neo4jConfigLoader();
	}

	@Bean
	public ServicesGroovyConfigLoader macServiceGroovyConfigLoader() {
		return new ServicesGroovyConfigLoader();
	}

	@Bean
	public HJsonConfigLoader macHJsonConfigLoader() {
		return new HJsonConfigLoader();
	}

	@Bean
	public SpringConfigLoader macSpringConfigLoader() {
		return new SpringConfigLoader();
	}

	@Bean
	public CompositeConfigLoader macCompositeConfigLoader() {
		CompositeConfigLoader cl = new CompositeConfigLoader();
		cl.addLoader(macServiceGroovyConfigLoader());
		cl.addLoader(macHJsonConfigLoader());
		cl.addLoader(new SpringConfigLoader());
		cl.addLoader(macNeo4jConfigLoader());
		return cl;
	}

	@Bean
	public Slf4jEventWriter macSlf4jEventWriter() {
		return new Slf4jEventWriter();
	}

	@Bean
	public EventSystem macEventSystem() {

		return new EventSystem();
	}
	
	@Bean
	public ProxyConfigManager macProxyConfigManager() {
		return new ProxyConfigManager();
	}
}
