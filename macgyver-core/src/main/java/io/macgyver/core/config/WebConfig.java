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

import io.macgyver.core.web.handlebars.DummyHandlebarsController;
import io.macgyver.core.web.handlebars.MacGyverMustacheTemplateLoader;
import io.macgyver.core.web.handlebars.MacGyverMustacheViewResolver;
import io.macgyver.core.web.mvc.CoreApiController;
import io.macgyver.core.web.mvc.HomeController;
import io.macgyver.core.web.mvc.MacgyverWeb;
import io.macgyver.core.web.vaadin.MacGyverUI;
import io.macgyver.core.web.vaadin.MacGyverVaadinServlet;
import io.macgyver.core.web.vaadin.ViewDecorators;
import io.macgyver.core.web.vaadin.views.admin.AdminPlugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.mustache.MustacheAutoConfiguration;
import org.springframework.boot.autoconfigure.mustache.MustacheEnvironmentCollector;
import org.springframework.boot.autoconfigure.mustache.MustacheProperties;
import org.springframework.boot.autoconfigure.mustache.MustacheResourceTemplateLoader;
import org.springframework.boot.autoconfigure.mustache.web.MustacheViewResolver;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Mustache.Collector;
import com.samskivert.mustache.Mustache.Compiler;
import com.samskivert.mustache.Mustache.TemplateLoader;

@Configuration
@ComponentScan(basePackageClasses = { HomeController.class })
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
@EnableGlobalMethodSecurity(securedEnabled = true, proxyTargetClass = true,prePostEnabled=true)
@EnableConfigurationProperties(MustacheProperties.class)
@EnableAutoConfiguration(exclude={MustacheAutoConfiguration.class})
public class WebConfig implements EnvironmentAware {

	Logger logger = LoggerFactory.getLogger(WebConfig.class);

	@Autowired
	private final org.springframework.core.io.ResourceLoader resourceLoader = new DefaultResourceLoader();

	@Autowired
	private Environment environment;

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
			public Object postProcessBeforeInitialization(Object bean,
					String beanName) throws BeansException {
				if (bean instanceof RequestMappingHandlerMapping
						&& "requestMappingHandlerMapping".equals(beanName)) {
					RequestMappingHandlerMapping m = ((RequestMappingHandlerMapping) bean);
					
					
					
				}

				return bean;
			}

			@Override
			public Object postProcessAfterInitialization(Object bean,
					String beanName) throws BeansException {
				return bean;
			}
		};
	}

	@Bean
	public MacgyverWeb macWebConfig() {
		return new MacgyverWeb();
	}




	@Bean
	public ServletRegistrationBean macVaadinServlet() {
		ServletRegistrationBean sb = new ServletRegistrationBean(new MacGyverVaadinServlet(), "/ui/*","/VAADIN/*");
		sb.addInitParameter("ui", MacGyverUI.class.getName());
	//	sb.addInitParameter("legacyPropertyToString","false");
		return sb;
	}
	
	@Bean
	public ViewDecorators macViewDecorators() {
		return new ViewDecorators();
	}
	
	@Bean
	public AdminPlugin macAdminUIDecorator() {
		return new AdminPlugin();
	}
	
	
	@Bean
	public MustacheResourceTemplateLoader macMustacheTemplateLoader() {
		
		return new MacGyverMustacheTemplateLoader();
		

	}
	
	
	@Bean
	public Mustache.Compiler macMustacheCompiler() {
		return Mustache.compiler().withLoader(macMustacheTemplateLoader())
				.withCollector(collector());
	}

	private Collector collector() {
		MustacheEnvironmentCollector collector = new MustacheEnvironmentCollector();
		collector.setEnvironment(this.environment);
		return collector;
	}


	@Autowired
	MustacheProperties mustacheProperties;
		
	@Bean
		public MacGyverMustacheViewResolver macMustacheViewResolver() {
			MacGyverMustacheViewResolver resolver = new MacGyverMustacheViewResolver();
			resolver.setPrefix(mustacheProperties.getPrefix());
			resolver.setSuffix(mustacheProperties.getSuffix());
			resolver.setCache(mustacheProperties.isCache());
			resolver.setViewNames(mustacheProperties.getViewNames());
			resolver.setContentType(mustacheProperties.getContentType());
			resolver.setCharset(mustacheProperties.getCharset());
			resolver.setCompiler(macMustacheCompiler());
			resolver.setOrder(Ordered.LOWEST_PRECEDENCE - 10);
			return resolver;
		}

	
	@Bean
	public DummyHandlebarsController macDummyHandlebarsController() {
		return new DummyHandlebarsController();
	}
}
