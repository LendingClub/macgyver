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
