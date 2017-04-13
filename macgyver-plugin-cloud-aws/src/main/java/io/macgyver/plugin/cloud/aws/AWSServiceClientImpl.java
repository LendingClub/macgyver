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

import java.lang.reflect.Constructor;
import java.util.List;

import org.lendingclub.mercator.aws.AWSScannerBuilder;
import org.lendingclub.mercator.aws.AllEntityScanner;
import org.lendingclub.mercator.core.Projector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import io.macgyver.core.Kernel;
import io.macgyver.core.MacGyverException;
import io.macgyver.core.service.ProxyConfig;

public class AWSServiceClientImpl implements AWSServiceClient {

	Logger logger = LoggerFactory.getLogger(AWSServiceClientImpl.class);

	AWSCredentialsProvider credentialsProvider;

	String accountId;
	List<Regions> regionList = ImmutableList.of();
	ProxyConfig proxyConfig;

	public AWSServiceClientImpl() {
		super();
	}

	public AWSServiceClientImpl(AWSCredentialsProvider p) {
		credentialsProvider = p;
	}

	public AWSServiceClientImpl(AWSCredentialsProvider p, String accountId) {
		credentialsProvider = p;
		this.accountId = accountId;
	}

	@Override
	public AWSCredentialsProvider getCredentialsProvider() {
		return credentialsProvider;
	}

	@Override
	public AmazonS3Client newS3Client() {
		return createClient(AmazonS3Client.class);
	}

	@Override
	public AmazonEC2Client createEC2Client() {
		return createClient(AmazonEC2Client.class);
	}

	@Override
	public AmazonEC2Client createEC2Client(Region region) {
		AmazonEC2Client client = createClient(AmazonEC2Client.class,region);
	
		return client;
	}

	@Override
	public AmazonEC2Client createEC2Client(Regions region) {
		return createEC2Client(Region.getRegion(region));
	}

	@Override
	public AmazonEC2Client createEC2Client(String name) {
		return createEC2Client(Regions.fromName(name));
	}

	

	@Override
	public <T extends AmazonWebServiceClient> T createClient(Class<? extends T> t) {

		return createClient(t, null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <T extends AmazonWebServiceClient> T createClient(Class<? extends T> t, Region region) {
		

			AwsClientBuilder builder = AWSServiceFactory.createBuilderForClient(t);
			
	
		
			ClientConfiguration myClientConfiguration = new ClientConfiguration();

			if (proxyConfig != null) {
				myClientConfiguration.setProxyHost(proxyConfig.getHost());
				myClientConfiguration.setProxyPort(proxyConfig.getPort());
				if (proxyConfig.getUsername().isPresent()) {
					myClientConfiguration.setProxyUsername(proxyConfig.getUsername().get());
				}
				if (proxyConfig.getPassword().isPresent()) {
					myClientConfiguration.setProxyUsername(proxyConfig.getPassword().get());
				}
				myClientConfiguration.setNonProxyHosts("169.254.169.254");
			}
			
			builder.withClientConfiguration(myClientConfiguration);
			
			
			if (region==null) {			
				region = Region.getRegion(Regions.US_EAST_1);
				logger.warn("region not specified...defaulting to {}",region);
			}
			else {
				builder = builder.withRegion(region.getName());
			}
			
			return (T) builder.build();

		
	}

	@Override
	public String getAccountId() {

		return accountId;
	}

	protected void setAccountId(String id) {
		this.accountId = id;
	}

	public AWSScannerBuilder createScannerBuilder() {
		// No need to set region or account. Account will be determined at
		// runtime. Region will be selected by the caller.
		return Kernel.getApplicationContext().getBean(Projector.class).createBuilder(AWSScannerBuilder.class)
				.withCredentials(getCredentialsProvider()).withFailOnError(false);
	}

	@Override
	public void scanRegion(String name) {
		scanRegion(Regions.fromName(name));
	}

	@Override
	public void scanRegion(Regions region) {
		createScannerBuilder().withRegion(region).build(AllEntityScanner.class).scan();
	}

	@Override
	public void scanRegion(Region region) {
		scanRegion(Regions.fromName(region.getName()));
	}

	public void scan() {
		for (Regions region : getConfiguredRegions()) {
			try {
				scanRegion(region);
			} catch (RuntimeException e) {
				logger.warn("problem scanning region: " + region, e);
			}
		}
	}

	/**
	 * This is public, but only on the implementation class.
	 * 
	 * @param list
	 */

	public synchronized void setConfiguredRegions(List<Regions> list) {
		if (list == null) {
			list = Lists.newArrayList();
		}
		regionList = ImmutableList.copyOf(list);
	}

	@Override
	public synchronized List<Regions> getConfiguredRegions() {
		return regionList;
	}

	public String toString() {
		return MoreObjects.toStringHelper(this).add("account", getAccountId()).toString();

	}

}
