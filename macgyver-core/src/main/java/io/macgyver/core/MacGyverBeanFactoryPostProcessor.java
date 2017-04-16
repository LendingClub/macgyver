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
package io.macgyver.core;

import groovy.lang.Binding;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.groovy.GroovyBeanDefinitionReader;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.Ordered;
import org.springframework.core.io.InputStreamResource;

import com.google.common.collect.Sets;

public class MacGyverBeanFactoryPostProcessor implements
		BeanFactoryPostProcessor, Ordered {

	Logger logger = LoggerFactory.getLogger(MacGyverBeanFactoryPostProcessor.class);
	

	public MacGyverBeanFactoryPostProcessor() {
	
	}

	@Override
	public void postProcessBeanFactory(
			ConfigurableListableBeanFactory beanFactory) throws BeansException {
		try {
			File beansGroovyFile = Bootstrap.getInstance().resolveConfig("springConfig.groovy");
			
		
		if (beansGroovyFile.exists()) {
			logger.info("adding beans from: {}",beansGroovyFile);
			GroovyBeanDefinitionReader gbdr = new GroovyBeanDefinitionReader(
					(BeanDefinitionRegistry) beanFactory);
			Binding b = new Binding();
			
			PropertiesAccessor pa = new PropertiesAccessor((AbstractBeanFactory)beanFactory);
			b.setProperty("properties", pa);
			gbdr.setBinding(b);
			
			InputStreamResource isr = new InputStreamResource(new FileInputStream(beansGroovyFile)) {

				@Override
				public String getFilename() {
					// GroovyBeanDefinitionLoader chokes without this
					return "springConfig.groovy";
				}
				
			};
			
			gbdr.loadBeanDefinitions(isr);
			
		}
		else {
			logger.info("groovy bean config file not found: {}",beansGroovyFile);
		}
		}
		catch (Exception e) {}
	}

	
	public class PropertiesAccessor implements Map<String, Object> {

	    private final AbstractBeanFactory beanFactory;

	    private final Map<String,String> cache = new ConcurrentHashMap<>();


	    public PropertiesAccessor(AbstractBeanFactory beanFactory) {
	        this.beanFactory = beanFactory;
	    }

	    public  String getProperty(String key) {
	        if(cache.containsKey(key)){
	            return cache.get(key);
	        }

	        String foundProp = null;
	        try {
	        
	            foundProp = beanFactory.resolveEmbeddedValue("${" + key.trim() + "}");
	        
	            cache.put(key,foundProp);
	        } catch (IllegalArgumentException ex) {
	           // ok - property was not found
	        }

	        return foundProp;
	    }

		@Override
		public int size() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean isEmpty() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean containsKey(Object key) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean containsValue(Object value) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Object get(Object key) {
			return getProperty(key.toString());
		}

		@Override
		public Object put(String key, Object value) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object remove(Object key) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void putAll(Map<? extends String, ? extends Object> m) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void clear() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Set<String> keySet() {
			return new HashSet<String>();
		}

		@Override
		public Collection<Object> values() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Set<java.util.Map.Entry<String, Object>> entrySet() {
			return Sets.newHashSet();
		}
	}


	@Override
	public int getOrder() {
		return 0;
	}
	
	
}
