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

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.macgyver.core.event.MacGyverEventPublisher;
import io.macgyver.plugin.ci.jenkins.JenkinsNotificationMessage.BuildNotificationMessage;

/**
 * This is a webhook for the Post Completed Build Results plugin.
 * 
 * https://wiki.jenkins-ci.org/display/JENKINS/Post+Completed+Build+Result+Plugin
 * 
 * @author rschoening
 *
 */
@Controller
@RequestMapping("/api/plugin/ci/jenkins")
public class PostCompletedBuildResultWebhook {

	@Autowired
	MacGyverEventPublisher publisher;
	
	Logger logger = LoggerFactory.getLogger(PostCompletedBuildResultWebhook.class);
	ObjectMapper mapper = new ObjectMapper();
	
	public PostCompletedBuildResultWebhook() {
	
	}

	
	
	@RequestMapping(value="/post-completed-build-result",method={
			RequestMethod.POST},produces="application/json")
	@PreAuthorize("permitAll")
	public  ResponseEntity<JsonNode> postBuildCompleted(HttpServletRequest request) {
		
		String url = request.getParameter("url");
		logger.info("url: "+url);
		
		ObjectNode payload = mapper.createObjectNode().put("url", url);
		publisher.createMessage().withMessageType(BuildNotificationMessage.class).withMessageBody(payload).publish();
		;
		// we can call back to <url>/api/json to get actual information about the build
		return ResponseEntity.ok(mapper.createObjectNode().put("status", "ok"));
	}
}
