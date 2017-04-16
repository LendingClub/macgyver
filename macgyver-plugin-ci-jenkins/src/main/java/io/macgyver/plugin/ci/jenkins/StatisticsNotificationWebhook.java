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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.macgyver.plugin.ci.jenkins;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.macgyver.core.event.MacGyverEventPublisher;
import io.macgyver.plugin.ci.jenkins.JenkinsNotificationMessage.BuildNotificationMessage;
import io.macgyver.plugin.ci.jenkins.JenkinsNotificationMessage.ProjectNotificationMessage;
import io.macgyver.plugin.ci.jenkins.JenkinsNotificationMessage.QueueNotificationMessage;

/**
 * Webhook for the Jenkins Statistics Notification Plugin
 * 
 * https://wiki.jenkins-ci.org/display/JENKINS/Statistics+Notification+Plugin
 * 
 * @author rschoening
 *
 */
@Controller
@RequestMapping("/api/plugin/ci/jenkins/statistics-notification")
public class StatisticsNotificationWebhook {

	@Autowired
	MacGyverEventPublisher publisher;

	Logger logger = LoggerFactory.getLogger(StatisticsNotificationWebhook.class);
	ObjectMapper mapper = new ObjectMapper();




	public StatisticsNotificationWebhook() {
		// TODO Auto-generated constructor stub
	}

	@RequestMapping(value = "/builds", method = { RequestMethod.POST,
			RequestMethod.PUT }, produces = "application/json")
	@PreAuthorize("permitAll")
	public ResponseEntity<JsonNode> builds(@RequestBody JsonNode payload, HttpServletRequest request)
			throws IOException {

		publisher.createMessage(BuildNotificationMessage.class).withMessageBody(payload).publish();
		return ResponseEntity.ok(mapper.createObjectNode().put("status", "ok"));
	}

	@RequestMapping(value = "/queues", method = { RequestMethod.POST,
			RequestMethod.PUT }, produces = "application/json")
	@PreAuthorize("permitAll")
	public ResponseEntity<JsonNode> statisticsNotificationQueues(@RequestBody JsonNode payload,
			HttpServletRequest request) throws IOException {

		publisher.createMessage(QueueNotificationMessage.class).withMessageBody(payload).publish();

		return ResponseEntity.ok(mapper.createObjectNode().put("status", "ok"));
	}

	@RequestMapping(value = "/projects", method = { RequestMethod.POST,
			RequestMethod.PUT }, produces = "application/json")
	@PreAuthorize("permitAll")
	public ResponseEntity<JsonNode> statisticsNotificationProjects(@RequestBody JsonNode payload,
			HttpServletRequest request) throws IOException {

		publisher.createMessage(ProjectNotificationMessage.class).withMessageBody(payload).publish();

		return ResponseEntity.ok(mapper.createObjectNode().put("status", "ok"));
	}

}
