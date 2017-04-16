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

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.codedeploy.AmazonCodeDeployClient;
import com.google.common.base.Splitter;

import io.macgyver.core.util.StandaloneServiceBuilder;

public class AWSServiceFactoryTest {

	public static final String DUMMY_ACCESS = "dummyaccess";
	public static final String DUMMY_SECRET = "dummysecret";

	@Test
	public void testX() {
		AWSServiceClient sf = StandaloneServiceBuilder
				.forServiceFactory(AWSServiceFactory.class)
				.property("accessKey", DUMMY_ACCESS)
				.property("secretKey", DUMMY_SECRET)
				.build(AWSServiceClient.class);

		Assertions.assertThat(sf).isNotNull();
		Assertions.assertThat(
				sf.getCredentialsProvider().getCredentials()
						.getAWSAccessKeyId()).isEqualTo(DUMMY_ACCESS);
		Assertions.assertThat(
				sf.getCredentialsProvider().getCredentials().getAWSSecretKey())
				.isEqualTo(DUMMY_SECRET);

		Assertions.assertThat(
				sf.createEC2Client(Region.getRegion(Regions.US_EAST_1)))
				.isNotNull();
	}

	@Test
	public void testAdHocService() {
		AWSServiceClient sf = StandaloneServiceBuilder
				.forServiceFactory(AWSServiceFactory.class)
				.property("accessKey", DUMMY_ACCESS)
				.property("secretKey", DUMMY_SECRET)
				.build(AWSServiceClient.class);

		Assertions.assertThat(sf).isNotNull();
		Assertions.assertThat(
				sf.getCredentialsProvider().getCredentials()
						.getAWSAccessKeyId()).isEqualTo(DUMMY_ACCESS);
		Assertions.assertThat(
				sf.getCredentialsProvider().getCredentials().getAWSSecretKey())
				.isEqualTo(DUMMY_SECRET);

		Assertions.assertThat(sf.createClient(AmazonCodeDeployClient.class))
				.isNotNull().isInstanceOf(AmazonCodeDeployClient.class);

	}

	@Test
	public void testRegion() {
		AWSServiceClient sf = StandaloneServiceBuilder
				.forServiceFactory(AWSServiceFactory.class)
				.property("accessKey", DUMMY_ACCESS)
				.property("secretKey", DUMMY_SECRET)
				.property("region", "us-west-1").build(AWSServiceClient.class);

	}

	@Test
	public void testInvalidRegion() {
		try {
			AWSServiceClient sf = StandaloneServiceBuilder
					.forServiceFactory(AWSServiceFactory.class)
					.property("accessKey", DUMMY_ACCESS)
					.property("secretKey", DUMMY_SECRET)
					.property("region", "us-foo-1")
					.build(AWSServiceClient.class);
		} catch (IllegalArgumentException e) {
			Assertions.assertThat(e).isInstanceOf(
					IllegalArgumentException.class);
		}

	}

	@Test
	public void testSplitter() {
		
		Assertions.assertThat(AWSServiceFactory.splitToRegionList("us-west-2,foo,us-east-1")).hasSize(2).containsExactly(Regions.US_WEST_2,Regions.US_EAST_1);
			
		
	}
}
