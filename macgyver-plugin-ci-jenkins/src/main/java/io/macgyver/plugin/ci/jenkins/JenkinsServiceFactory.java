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
package io.macgyver.plugin.ci.jenkins;

import java.util.Set;

import org.lendingclub.projector.core.Projector;
import org.lendingclub.projector.jenkins.JenkinsScannerBuilder;

import com.google.common.base.Strings;

import io.macgyver.core.Kernel;
import io.macgyver.core.service.ServiceDefinition;
import io.macgyver.core.service.ServiceFactory;
import io.macgyver.core.service.ServiceRegistry;

public class JenkinsServiceFactory extends ServiceFactory<JenkinsClient> {

	public JenkinsServiceFactory() {
		super("jenkins");

	}

	@Override
	protected JenkinsClient doCreateInstance(ServiceDefinition def) {
		JenkinsClientImpl c = new JenkinsClientImpl(def.getProperties()
				.getProperty("url"), def.getProperties()
				.getProperty("username"), def.getProperties().getProperty(
				"password"));

		return c;
	}

	@Override
	protected void doCreateCollaboratorInstances(ServiceRegistry registry, ServiceDefinition primaryDefinition,
			Object primaryBean) {
		JenkinsScannerBuilder builder = Kernel.getApplicationContext().getBean(Projector.class).createBuilder(JenkinsScannerBuilder.class)
		.withUrl(primaryDefinition.getProperty("url"));
		
		String username = primaryDefinition.getProperty("username");
		String password = primaryDefinition.getProperty("password");
		
		if (!Strings.isNullOrEmpty(username)) {
			builder = builder.withUsername(username);
		}
		if (!Strings.isNullOrEmpty(password)) {
			builder = builder.withPassword(password);
		}
		org.lendingclub.projector.jenkins.JenkinsScanner scanner = builder.build();
		
		registry.registerCollaborator(primaryDefinition.getName()+"Scanner", scanner);
	}


	@Override
	public void doCreateCollaboratorDefinitions(Set<ServiceDefinition> defSet,
			ServiceDefinition def) {
		ServiceDefinition templateDef = new ServiceDefinition(def.getName()
				+ "Scanner", def.getName(), def.getProperties(), this);
		defSet.add(templateDef);
	}

}
