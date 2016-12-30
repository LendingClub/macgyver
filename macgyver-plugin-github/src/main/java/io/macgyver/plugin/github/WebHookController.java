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
package io.macgyver.plugin.github;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.lendingclub.reflex.consumer.Consumers;
import org.lendingclub.reflex.predicate.Predicates;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;

import io.macgyver.core.event.EventSystem;
import io.macgyver.core.event.MacGyverEventPublisher;

@Controller
public class WebHookController {

	public static final int WEBHOOK_MAX_BYTES_DEFAULT = 500 * 1024;

	@Autowired
	MacGyverEventPublisher eventPublisher;

	@Autowired
	EventSystem eventSystem;

	static org.slf4j.Logger logger = LoggerFactory
			.getLogger(WebHookController.class);

	ObjectMapper mapper = new ObjectMapper();

	List<WebHookAuthenticator> authenticatorList = new CopyOnWriteArrayList<>();

	int webhookMaxBytes = WEBHOOK_MAX_BYTES_DEFAULT;

	@PostConstruct
	public void registerLogger() {
		eventSystem.getObservable().filter(Predicates.type(GitHubWebHookMessage.class)).subscribe(
				Consumers.safeConsumer(c -> {
					logger.info("received {}", c);
				}));
	}

	@RequestMapping(value = "/api/plugin/github/webhook", method = RequestMethod.POST, consumes = "application/json")
	@PreAuthorize("permitAll")
	@ResponseBody
	public ResponseEntity<JsonNode> processHook(HttpServletRequest request)
			throws IOException, InvalidKeyException, NoSuchAlgorithmException {

		if (request.getContentLength() > webhookMaxBytes) {
			JsonNode returnNode = new ObjectMapper().createObjectNode()
					.put("success", "false")
					.put("error", "message too large");
			return new ResponseEntity<JsonNode>(returnNode,
					HttpStatus.UNAUTHORIZED);
		}

		GitHubWebHookMessage event = new GitHubWebHookMessage(request);

		if (isAuthenticated(event)) {

			eventPublisher.createMessage().withMessage(event).publish();

			JsonNode returnNode = new ObjectMapper().createObjectNode().put(
					"success", "true");
			return new ResponseEntity<JsonNode>(returnNode, HttpStatus.OK);
		} else {
			JsonNode returnNode = new ObjectMapper().createObjectNode()
					.put("success", "false")
					.put("error", "unauthorized");
			return new ResponseEntity<JsonNode>(returnNode,
					HttpStatus.UNAUTHORIZED);
		}

	}

	boolean isAuthenticated(GitHubWebHookMessage event) {

		if (authenticatorList == null || authenticatorList.isEmpty()) {
			// if no authenticators are set up, assume we want to just trust
			// everything
			return true;
		}

		for (WebHookAuthenticator auth : authenticatorList) {

			java.util.Optional<Boolean> b = auth.authenticate(event);
			if (b.isPresent() && (!b.get().booleanValue())) {
				return b.get();
			}
		}
		return true;
	}

	public void addAuthenticator(WebHookAuthenticator auth) {
		Preconditions.checkNotNull(auth);
		authenticatorList.add(auth);
	}
}
