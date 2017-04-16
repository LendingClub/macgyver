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
package io.macgyver.plugin.cloud.aws;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;

import io.macgyver.core.service.ServiceRegistry;

public class AWSServiceClientAssumeRoleCredentialsProvider implements AWSCredentialsProvider {

	private final ServiceRegistry registry;
	private final String sourceServiceName;
	private final String roleArn;
	// this is going to be a mouthful...
	private volatile STSAssumeRoleSessionCredentialsProvider stsAssumeRoleSessionCredentialsProvider;

	public AWSServiceClientAssumeRoleCredentialsProvider(ServiceRegistry registry, String sourceServiceName,
			String roleArn) {
		this.registry = registry;
		this.sourceServiceName = sourceServiceName;
		this.roleArn = roleArn;
	}

	@Override
	public AWSCredentials getCredentials() {
		if (stsAssumeRoleSessionCredentialsProvider == null) {
			synchronized (this) {
				if (stsAssumeRoleSessionCredentialsProvider == null) {
					/*
					 * We defer the lookup of the source service until we
					 * actually need it
					 */
					AWSServiceClient sourceService = registry.get(sourceServiceName, AWSServiceClient.class);
					String roleSessionName = System.getProperty("user.name");
					try {
						String host = InetAddress.getLocalHost().getHostName();
						roleSessionName = roleSessionName + "@" + host;
					} catch (UnknownHostException e) {
					}
					AWSSecurityTokenServiceClient sts = sourceService.createClient(AWSSecurityTokenServiceClient.class);
					stsAssumeRoleSessionCredentialsProvider = new STSAssumeRoleSessionCredentialsProvider.Builder(
							roleArn, roleSessionName).withStsClient(sts).build();
				}
			}
		}
		return stsAssumeRoleSessionCredentialsProvider.getCredentials();
	}

	@Override
	public void refresh() {
		if (stsAssumeRoleSessionCredentialsProvider != null) {
			stsAssumeRoleSessionCredentialsProvider.refresh();
		}
	}

}
