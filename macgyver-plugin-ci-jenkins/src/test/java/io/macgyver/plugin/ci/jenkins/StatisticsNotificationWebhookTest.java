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
package io.macgyver.plugin.ci.jenkins;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class StatisticsNotificationWebhookTest {

	public StatisticsNotificationWebhookTest() {
		// TODO Auto-generated constructor stub
	}

	String config = "{\"name\":\"testjob-1\",\"userId\":\"rschoening\",\"userName\":\"Rob Schoening\",\"ciUrl\":\"https://ci.example.com/\",\"updatedDate\":\"Sep 27, 2015 3:36:23 PM\",\"status\":\"ACTIVE\",\"configFile\":\"\\u003c?xml version\\u003d\\u00271.0\\u0027 encoding\\u003d\\u0027UTF-8\\u0027?\\u003e\\n\\u003cproject\\u003e\\n  \\u003cactions/\\u003e\\n  \\u003cdescription\\u003e\\u003c/description\\u003e\\n  \\u003ckeepDependencies\\u003efalse\\u003c/keepDependencies\\u003e\\n  \\u003cproperties\\u003e\\n    \\u003chudson.plugins.throttleconcurrents.ThrottleJobProperty plugin\\u003d\\\"throttle-concurrents@1.8.4\\\"\\u003e\\n      \\u003cmaxConcurrentPerNode\\u003e0\\u003c/maxConcurrentPerNode\\u003e\\n      \\u003cmaxConcurrentTotal\\u003e0\\u003c/maxConcurrentTotal\\u003e\\n      \\u003cthrottleEnabled\\u003efalse\\u003c/throttleEnabled\\u003e\\n      \\u003cthrottleOption\\u003eproject\\u003c/throttleOption\\u003e\\n    \\u003c/hudson.plugins.throttleconcurrents.ThrottleJobProperty\\u003e\\n  \\u003c/properties\\u003e\\n  \\u003cscm class\\u003d\\\"hudson.scm.NullSCM\\\"/\\u003e\\n  \\u003ccanRoam\\u003etrue\\u003c/canRoam\\u003e\\n  \\u003cdisabled\\u003efalse\\u003c/disabled\\u003e\\n  \\u003cblockBuildWhenDownstreamBuilding\\u003efalse\\u003c/blockBuildWhenDownstreamBuilding\\u003e\\n  \\u003cblockBuildWhenUpstreamBuilding\\u003efalse\\u003c/blockBuildWhenUpstreamBuilding\\u003e\\n  \\u003cjdk\\u003e(Default)\\u003c/jdk\\u003e\\n  \\u003ctriggers/\\u003e\\n  \\u003cconcurrentBuild\\u003efalse\\u003c/concurrentBuild\\u003e\\n  \\u003cbuilders\\u003e\\n    \\u003chudson.tasks.Shell\\u003e\\n      \\u003ccommand\\u003eecho test\\n\\nexit 1\\u003c/command\\u003e\\n    \\u003c/hudson.tasks.Shell\\u003e\\n  \\u003c/builders\\u003e\\n  \\u003cpublishers/\\u003e\\n  \\u003cbuildWrappers/\\u003e\\n\\u003c/project\\u003e\"}"
		;
	String queueEnter = "{\"ciUrl\":\"https://ci.example.com/\",\"jobName\":\"ops-purge-artifacts\",\"entryTime\":\"Sep 27, 2015 3:31:00 PM\",\"startedBy\":\"TIMER\",\"jenkinsQueueId\":10535,\"status\":\"ENTERED\",\"duration\":0,\"queueCauses\":{\"waiting\":{\"entryTime\":\"Sep 27, 2015 3:31:00 PM\",\"reasonForWaiting\":\"???\"}}}";
	String queueExit  = "{\"ciUrl\":\"https://ci.example.com/\",\"jobName\":\"ops-purge-artifacts\",\"exitTime\":\"Sep 27, 2015 3:31:00 PM\",\"jenkinsQueueId\":10535,\"status\":\"LEFT\",\"duration\":247,\"durationStr\":\"0.24 sec\"}";
	
	String buildStart = "{\n" + 
			"  \"ciUrl\" : \"https://ci.example.com/\",\n" + 
			"  \"jobName\" : \"testjob-1\",\n" + 
			"  \"number\" : 35,\n" + 
			"  \"slaveInfo\" : {\n" + 
			"    \"slaveName\" : \"buildslave01\",\n" + 
			"    \"label\" : \"linux-slave\",\n" + 
			"    \"remoteFs\" : \"jenkins\"\n" + 
			"  },\n" + 
			"  \"startTime\" : \"Sep 27, 2015 3:28:08 PM\",\n" + 
			"  \"startedUserId\" : \"rschoening\",\n" + 
			"  \"startedUserName\" : \"Rob Schoening\",\n" + 
			"  \"result\" : \"INPROGRESS\",\n" + 
			"  \"duration\" : 0,\n" + 
			"  \"scmInfo\" : { },\n" + 
			"  \"queueTime\" : 40\n" + 
			"}";
	
	String buildResult="{\n" + 
			"  \"ciUrl\" : \"https://ci.example.com/\",\n" + 
			"  \"jobName\" : \"testjob-1\",\n" + 
			"  \"number\" : 35,\n" + 
			"  \"endTime\" : \"Sep 27, 2015 3:28:08 PM\",\n" + 
			"  \"result\" : \"FAILURE\",\n" + 
			"  \"duration\" : 124,\n" + 
			"  \"queueTime\" : 0\n" + 
			"}";
	
	@Test
	public void testConfig() throws IOException {
		
		ObjectMapper mapper = new ObjectMapper();
		System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(new ObjectMapper().readTree(config)));
	}
}
