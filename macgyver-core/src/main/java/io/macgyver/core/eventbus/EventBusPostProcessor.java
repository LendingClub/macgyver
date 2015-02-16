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
package io.macgyver.core.eventbus;

import io.macgyver.core.ContextRefreshApplicationListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.eventbus.EventBus;

public class EventBusPostProcessor implements BeanPostProcessor,
		ApplicationContextAware {

	Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	MacGyverEventBus eventBus;

	@Autowired
	MacGyverAsyncEventBus asyncBus;
	
	private ApplicationContext ctx;

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {

		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {

		if (bean!=this && !(bean instanceof EventBus) && !(bean instanceof ContextRefreshApplicationListener)) {
			Package p = bean.getClass().getPackage();
			
			if (p!=null) {
				log.trace("registering spring bean {} with {}", beanName,
						eventBus);
				eventBus.register(bean);
				asyncBus.register(bean);
			}
		}
		return bean;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.ctx = applicationContext;

	}

}