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
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import org.lendingclub.projector.aws.AWSScannerBuilder;
import org.lendingclub.projector.core.Projector;

import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.s3.AmazonS3Client;

import io.macgyver.core.Kernel;
import io.macgyver.core.MacGyverException;

public class AWSServiceClientImpl implements AWSServiceClient {

	AWSCredentialsProvider credentialsProvider;
	Region defaultRegion=null;
	String accountId;
	
	public  AWSServiceClientImpl() {
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
		return createClient(AmazonS3Client.class,defaultRegion);
	}

	@Override
	public AmazonEC2Client createEC2Client() {
		return createClient(AmazonEC2Client.class,defaultRegion);
	}

	@Override
	public AmazonEC2Client createEC2Client(Region region) {
		AmazonEC2Client client = createEC2Client();
		assignRegion(client, region);
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

	void assignRegion(AmazonWebServiceClient c, String r) {
		assignRegion(c, Regions.fromName(r));
	}

	void assignRegion(AmazonWebServiceClient c, Regions r) {
		c.setRegion(Region.getRegion(r));
	}

	void assignRegion(AmazonWebServiceClient c, Region r) {
		c.setRegion(r);
	}

	@Override
	public <T extends AmazonWebServiceClient> T createClient(
			Class<? extends T> t) {

		return createClient(t, defaultRegion);
	}

	public Optional<Region> getDefaultRegion() {
		return Optional.ofNullable(defaultRegion);
	}
	
	@Override
	public <T extends AmazonWebServiceClient> T createClient(
			Class<? extends T> t, Region region) {
		try {

			Constructor<?> constructor = (Constructor<?>) t
					.getDeclaredConstructor(AWSCredentialsProvider.class);
			T client = t.cast(constructor.newInstance(getCredentialsProvider()));

			if (region != null) {
				assignRegion(client, region);
			}

			return client;
		} catch (IllegalAccessException | InstantiationException
				| InvocationTargetException | NoSuchMethodException e) {
			throw new MacGyverException("could not create AWS client: " + t, e);
		}
	}

	@Override
	public String getAccountId() {
		
		return accountId;
	}
	
	protected void setAccountId(String id) {
		this.accountId = id;
	}

	public AWSScannerBuilder createScannerBuilder() {
		// No need to set region or account.  Account will be determined at runtime.  Region will be selected by the caller.
		return Kernel.getApplicationContext().getBean(Projector.class).createBuilder(AWSScannerBuilder.class).withCredentials(getCredentialsProvider());
	}
}
