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
package io.macgyver.ldap;

import io.macgyver.core.MacGyverException;
import io.macgyver.core.service.ServiceDefinition;
import io.macgyver.core.service.ServiceFactory;
import io.macgyver.core.service.ServiceRegistry;

import java.util.Properties;
import java.util.Set;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import com.google.common.base.Throwables;

public class LdapServiceFactory extends ServiceFactory<LdapContextSource> {

	public LdapServiceFactory(String name) {
		super(name);
	}

	@Override
	public Object doCreateInstance(ServiceDefinition def) {
		try {
			Properties props = def.getProperties();
			LdapContextSource cs = new LdapContextSource();

			org.springframework.beans.BeanWrapper gw = new BeanWrapperImpl(cs);

			assignProperties(cs, props, true);

			cs.afterPropertiesSet();
			return cs;
		} catch (Exception e) {
			Throwables.propagateIfPossible(e);
			throw new MacGyverException(e);
		}
	}

	@Override
	protected void doCreateCollaboratorInstances(ServiceRegistry registry,
			ServiceDefinition primaryDefinition, Object primaryBean) {
		LdapTemplate template = new LdapTemplate(
				(LdapContextSource) primaryBean);
		registry.registerCollaborator(primaryDefinition.getName() + "Template",
				template);

	}

	@Override
	public void doCreateCollaboratorDefinitions(Set<ServiceDefinition> defSet,
			ServiceDefinition def) {
		ServiceDefinition templateDef = new ServiceDefinition(def.getName()
				+ "Template", def.getName(), def.getProperties(), this);
		defSet.add(templateDef);

	}

}
