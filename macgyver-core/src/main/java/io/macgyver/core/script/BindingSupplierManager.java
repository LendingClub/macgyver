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
package io.macgyver.core.script;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

public class BindingSupplierManager implements BindingSupplier{

	@Autowired
	ApplicationContext applicationContext;
	
	Logger logger = LoggerFactory.getLogger(BindingSupplierManager.class);
	
	public void collect(Map<String, Object> c, Optional<String> lang) {
		
		Map<String,BindingSupplier> map = applicationContext.getBeansOfType(BindingSupplier.class);
		for (Map.Entry<String, BindingSupplier> m: map.entrySet()) {
			try {
				if (m.getValue()!=this) {
					logger.trace("obtaining bindings from supplier: {}",m.getKey());
					m.getValue().collect(c,lang);
				}
				
				
			}
			catch (RuntimeException e) {
				logger.warn("problem processing binding supplier",e);
			}
		}
	}
	
	public Map<String,Object> collect(Optional<String> lang) {
		Map<String,Object> m = Maps.newConcurrentMap();
		collect(m,lang);
		return m;
	}
}
