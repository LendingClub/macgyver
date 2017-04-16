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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.util.StringUtils;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import io.macgyver.core.MacGyverConfigurationException;
import io.macgyver.core.service.ServiceDefinition;
import io.macgyver.core.service.ServiceFactory;

public class AWSServiceFactory extends ServiceFactory<AWSServiceClient> {

	public AWSServiceFactory() {
		super("aws");

	}

	public static List<Regions> splitToRegionList(String input) {
		List<Regions> regionList = Lists.newArrayList();
		Splitter.onPattern("(,|\\s|;)").omitEmptyStrings().trimResults()
		.splitToList(Strings.nullToEmpty(input)).forEach(name->{
			try {
				regionList.add(Regions.fromName(name));
			} catch (RuntimeException e) {
				LoggerFactory.getLogger(AWSServiceFactory.class).warn("could not configure region: " + name, e);
			}
		});
		return regionList;
	}
	@Override
	protected AWSServiceClient doCreateInstance(ServiceDefinition def) {

		AWSServiceClientImpl ci = new AWSServiceClientImpl();
		ci.proxyConfig = def.getProxyConfig().orElse(null);
		ci.setAccountId(def.getProperty("accountId"));

		ci.credentialsProvider = getCredentialsProvider(def);
		
		
		ci.setConfiguredRegions(splitToRegionList(def.getProperties().getProperty("regions")));
		
		return ci;
	}

	
	private AWSCredentialsProvider getCredentialsProvider(ServiceDefinition def) {

		String accessKey = def.getProperty("accessKey");
		String secretKey = def.getProperty("secretKey");

		if (!StringUtils.isNullOrEmpty(accessKey) && !StringUtils.isNullOrEmpty(secretKey)) {
			logger.info("using static credentials " + accessKey + " for AWS service " + def.getName());
			return new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));
		}

		String sourceService = def.getProperty("sourceService");
		String assumeRoleName = def.getProperty("assumeRoleName");
		if (!StringUtils.isNullOrEmpty(sourceService) && !StringUtils.isNullOrEmpty(assumeRoleName)) {
			String roleArn = "arn:aws:iam::" + def.getProperty("accountId") + ":role/" + assumeRoleName;
			logger.info("using assume-role credentials for " + roleArn + " from " + sourceService + " for AWS service "
					+ def.getName());
			return new AWSServiceClientAssumeRoleCredentialsProvider(registry, sourceService, roleArn);
		}

		logger.info("using default credentials provider for AWS service " + def.getName());
		return new DefaultAWSCredentialsProviderChain();

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T extends AmazonWebServiceClient> AwsClientBuilder<AwsClientBuilder,T> createBuilderForClient(Class<? extends T> clientClass) {
	
		com.google.common.base.Preconditions.checkNotNull(clientClass);
		try {
			
			String clientClassName = clientClass.getName();
			String builderClassName = clientClassName + "Builder";
	
			Class<? extends AwsClientBuilder> builderClass = (Class<? extends AwsClientBuilder>) Class.forName(builderClassName);
		
			Method m = builderClass.getMethod("standard");
			
			return (AwsClientBuilder) m.invoke(builderClass);
		
		} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new MacGyverConfigurationException(e);
		}
	
	}

}
