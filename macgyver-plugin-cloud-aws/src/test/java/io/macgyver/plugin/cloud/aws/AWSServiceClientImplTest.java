package io.macgyver.plugin.cloud.aws;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.AmazonAutoScalingAsyncClient;
import com.amazonaws.services.autoscaling.AmazonAutoScalingAsyncClientBuilder;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClientBuilder;
import com.amazonaws.services.codedeploy.AmazonCodeDeploy;
import com.amazonaws.services.codedeploy.AmazonCodeDeployAsyncClient;
import com.amazonaws.services.codedeploy.AmazonCodeDeployAsyncClientBuilder;
import com.amazonaws.services.codedeploy.AmazonCodeDeployClient;
import com.amazonaws.services.codedeploy.AmazonCodeDeployClientBuilder;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2AsyncClient;
import com.amazonaws.services.ec2.AmazonEC2AsyncClientBuilder;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSAsyncClient;
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

public class AWSServiceClientImplTest {

	@Test
	public void testSync() {
		Assertions.assertThat(AWSServiceFactory.createBuilderForClient(AmazonEC2Client.class)).isInstanceOf(AmazonEC2ClientBuilder.class);
		Assertions.assertThat(AWSServiceFactory.createBuilderForClient(AmazonS3Client.class)).isInstanceOf(AmazonS3ClientBuilder.class);
		Assertions.assertThat(AWSServiceFactory.createBuilderForClient(AmazonSNSClient.class)).isInstanceOf(AmazonSNSClientBuilder.class);
		Assertions.assertThat(AWSServiceFactory.createBuilderForClient(AmazonCodeDeployClient.class)).isInstanceOf(AmazonCodeDeployClientBuilder.class);
		Assertions.assertThat(AWSServiceFactory.createBuilderForClient(AmazonSQSClient.class)).isInstanceOf(AmazonSQSClientBuilder.class);
		Assertions.assertThat(AWSServiceFactory.createBuilderForClient(AmazonAutoScalingClient.class)).isInstanceOf(AmazonAutoScalingClientBuilder.class);
	}
	@Test
	public void testAsync() {
		Assertions.assertThat(AWSServiceFactory.createBuilderForClient(AmazonEC2AsyncClient.class)).isInstanceOf(AmazonEC2AsyncClientBuilder.class);

		Assertions.assertThat(AWSServiceFactory.createBuilderForClient(AmazonSNSAsyncClient.class)).isInstanceOf(AmazonSNSAsyncClientBuilder.class);
		Assertions.assertThat(AWSServiceFactory.createBuilderForClient(AmazonCodeDeployAsyncClient.class)).isInstanceOf(AmazonCodeDeployAsyncClientBuilder.class);
		Assertions.assertThat(AWSServiceFactory.createBuilderForClient(AmazonSQSAsyncClient.class)).isInstanceOf(AmazonSQSAsyncClientBuilder.class);
		Assertions.assertThat(AWSServiceFactory.createBuilderForClient(AmazonAutoScalingAsyncClient.class)).isInstanceOf(AmazonAutoScalingAsyncClientBuilder.class);
	}

}
