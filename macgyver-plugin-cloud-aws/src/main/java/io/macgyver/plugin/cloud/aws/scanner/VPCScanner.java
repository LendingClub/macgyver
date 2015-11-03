package io.macgyver.plugin.cloud.aws.scanner;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.regions.Region;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeVpcsResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;

import io.macgyver.neorx.rest.NeoRxClient;
import io.macgyver.plugin.cloud.aws.AWSServiceClient;

public class VPCScanner extends AWSServiceScanner {

	Logger logger = LoggerFactory.getLogger(VPCScanner.class);

	public VPCScanner(AWSServiceClient client, NeoRxClient neo4j) {
		super(client, neo4j);

	}

	@Override
	public void scan(Region region) {

		AmazonEC2Client c = getAWSServiceClient().createEC2Client(region);

		DescribeVpcsResult result = c.describeVpcs();

		NeoRxClient neoRx = getNeoRxClient();
		Preconditions.checkNotNull(neoRx);

		result.getVpcs().forEach(it -> {
			try {					
				ObjectNode n = convertAwsObject(it, region);
									
				String cypher = "match (y:AwsSubnet {aws_vpcId:{aws_vpcId}}) "
						+ "merge (x:AwsVpc {aws_arn:{aws_arn}}) set x+={props} set x.updateTs=timestamp() "
						+ "merge (x)-[r:CONTAINS]->(y) set r.updateTs=timestamp()";
				
				neoRx.execCypher(cypher, "aws_arn",n.path("aws_arn").asText(), "aws_vpcId",n.path("aws_vpcId").asText(), "props",n);	
			} catch (RuntimeException e) { 
				logger.warn("problem scanning VPC", e);
			}
		});
	
		String mapAccountCypher = "match (x:AwsAccount {aws_account:{aws_account}}), (y:AwsVpc {aws_account:{aws_account}}) "
				+ "merge (x)-[r:OWNS]->(y) set r.updateTs=timestamp()";
		String mapRegionCypher = "match (v:AwsVpc {aws_region:{aws_region}}), (r:AwsRegion {aws_regionName:{aws_region}}) "
				+ "merge (r)-[r:CONTAINS]->(v) set r.updateTs=timestamp()";
		
		neoRx.execCypher(mapAccountCypher, "aws_account",getAWSServiceClient().getAccountId());
		neoRx.execCypher(mapRegionCypher, "aws_region", region.getName());
	}
	

	@Override
	public Optional<String> computeArn(JsonNode n) {

		String region = n.get("aws_region").asText();

		return Optional.of(String.format("arn:aws:ec2:%s:%s:vpc/%s", region, n.get("aws_account").asText(),
				n.get("aws_vpcId").asText()));
	}
}