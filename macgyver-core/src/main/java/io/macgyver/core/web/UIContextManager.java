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
package io.macgyver.core.web;

import java.util.Map;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class UIContextManager {

	Logger logger = LoggerFactory.getLogger(UIContextManager.class);
	@Autowired
	ApplicationContext applicationContext;

	public UIContext forCurrentUser() {

		
		try {
		MacGyverWebContext ctx = MacGyverWebContext.get();
		
		UIContext nc = (UIContext) ctx.getServletRequest().getSession(true).getAttribute(UIContext.class.getName());
		
		if (nc==null) {
			
			Map<String, UIContextDecorator> beans = applicationContext
					.getBeansOfType(UIContextDecorator.class);

			final UIContext ncf  = new UIContext();
			beans.entrySet().stream().forEach(kv -> {
				logger.info(kv.getKey()+ " ==> "+kv.getValue());
				
				kv.getValue().call(ncf);
			});
			
			ctx.getServletRequest().getSession(true).setAttribute(UIContext.class.getName(), ncf);
			nc = ncf;
		}
		nc.sort();
		return nc;
		}
		catch (RuntimeException e) {
			return new UIContext();
		}
	}

}
