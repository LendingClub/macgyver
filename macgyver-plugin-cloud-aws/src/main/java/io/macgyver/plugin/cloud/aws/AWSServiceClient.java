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

import java.util.List;
import java.util.Optional;

import org.lendingclub.mercator.aws.AWSScannerBuilder;

import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.s3.AmazonS3Client;

/**
 * Client factory for creating instances of the AWS Java SDK.
 * @author rschoening
 *
 */
public interface AWSServiceClient {

	/**
	 * Returns a credentials provider that returns credentials through the MacGyver service configuration
	 * system.
	 */
	AWSCredentialsProvider getCredentialsProvider();
	
	
	/**
	 * Convenience for creating an AWS service specific client.  This provides an alternative to manually instantiating
	 * the AWS-provided client and passing the AWSCredentialsProvider.
	 */
	<T extends AmazonWebServiceClient> T createClient(Class<? extends T> t);
	<T extends AmazonWebServiceClient> T createClient(Class<? extends T> t, Region region);
	
	
	List<Regions> getConfiguredRegions();
	
	AmazonS3Client newS3Client();
	
	AmazonEC2Client createEC2Client();
	AmazonEC2Client createEC2Client(String name);
	AmazonEC2Client createEC2Client(Regions region);
	AmazonEC2Client createEC2Client(Region region);
	
	AWSScannerBuilder createScannerBuilder();
	void scanRegion(String name);
	void scanRegion(Regions region);
	void scanRegion(Region region);
	
	String getAccountId();
	
	

}
