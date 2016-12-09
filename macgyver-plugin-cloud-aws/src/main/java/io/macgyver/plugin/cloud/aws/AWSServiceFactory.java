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

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.util.StringUtils;
import com.google.common.base.Strings;

import io.macgyver.core.service.ServiceDefinition;
import io.macgyver.core.service.ServiceFactory;

public class AWSServiceFactory extends ServiceFactory<AWSServiceClient> {

	public AWSServiceFactory() {
		super("aws");

	}

	@Override
	protected AWSServiceClient doCreateInstance(ServiceDefinition def) {

		AWSServiceClientImpl ci = new AWSServiceClientImpl();
		ci.setAccountId(def.getProperty("accountId"));

		ci.credentialsProvider = getCredentialsProvider(def);
		String regionName = Strings.emptyToNull(Strings.nullToEmpty(def.getProperties().getProperty("region")).trim());
		if (regionName != null) {
			logger.info("setting region: {}", regionName);
			ci.defaultRegion = Region.getRegion(Regions.fromName(regionName));
		}

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

}
