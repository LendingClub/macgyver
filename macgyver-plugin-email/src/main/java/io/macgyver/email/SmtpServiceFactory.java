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
package io.macgyver.email;

import io.macgyver.core.service.ServiceDefinition;
import io.macgyver.core.service.ServiceRegistry;

import java.util.Properties;
import java.util.Set;

import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class SmtpServiceFactory extends
		io.macgyver.core.service.ServiceFactory<Session> {

	public SmtpServiceFactory() {
		super("smtp");
	}

	@Override
	protected Object doCreateInstance(ServiceDefinition def) {
		final Properties props = def.getProperties();
		String host = props.getProperty("host");
		boolean authEnabled = Boolean.parseBoolean(props.getProperty("auth"));
		Preconditions.checkArgument(!Strings.isNullOrEmpty(host),
				"host must be set");

		props.put("mail.smtp.auth", props.getProperty("auth", "false"));
		props.put("mail.smtp.starttls.enable",
				props.getProperty("starttls.enable", "false"));
		props.put("mail.smtp.host", props.getProperty("host"));
		props.put("mail.smtp.port", props.getProperty("port", "25"));

		if (authEnabled) {
			Session session = Session.getInstance(props,
					new javax.mail.Authenticator() {
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(props
									.getProperty("username"), props
									.getProperty("password"));
						}
					});

			return session;
		} else {
			Session session = Session.getInstance(props);
			return session;
		}

	}





	@Override
	protected void doCreateCollaboratorInstances(
			ServiceRegistry registry,
			ServiceDefinition primaryDefinition, Object primaryBean) {
		
		registry.registerCollaborator(primaryDefinition.getName()+"Client", new SmtpClient((Session)primaryBean));
	}

	@Override
	public void doCreateCollaboratorDefinitions(Set<ServiceDefinition> defSet,
			ServiceDefinition def) {
		ServiceDefinition collab = new ServiceDefinition(def.getName()+"Client", def.getName(), def.getProperties(), this);
		defSet.add(collab);
	}

}
