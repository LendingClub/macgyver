package io.macgyver.plugin.github;

import io.macgyver.core.eventbus.MacGyverAsyncEventBus;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

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
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.ByteStreams;

@Controller
public class WebHookController {

	public static final int WEBHOOK_MAX_BYTES_DEFAULT=500 * 1024;
	
	@Autowired
	MacGyverAsyncEventBus eventBus;

	static org.slf4j.Logger logger = LoggerFactory
			.getLogger(WebHookController.class);

	ObjectMapper mapper = new ObjectMapper();

	List<WebHookAuthenticator> authenticatorList = new CopyOnWriteArrayList<>();
	
	int webhookMaxBytes = WEBHOOK_MAX_BYTES_DEFAULT;
	
	public static class WebHookLogReceiver {
		@Subscribe
		public void testIt(WebHookEvent evt) {
			logger.info("received: {}", evt);
		}
	}

	@PostConstruct
	public void registerLogger() {
		eventBus.register(new WebHookLogReceiver());
	}

	@RequestMapping(value = "/api/plugin/github/webhook", method = RequestMethod.POST, consumes = "application/json")
	@PreAuthorize("permitAll")
	@ResponseBody
	public ResponseEntity<JsonNode> processHook(HttpServletRequest request)
			throws IOException, InvalidKeyException, NoSuchAlgorithmException {

		if (request.getContentLength() >webhookMaxBytes) {
			JsonNode returnNode = new ObjectMapper().createObjectNode()
					.put("success", "false")
					.put("error", "message too large");
			return new ResponseEntity<JsonNode>(returnNode,
					HttpStatus.UNAUTHORIZED);	
		}
		
		byte[] bytes = ByteStreams.toByteArray(request.getInputStream());

		WebHookEvent event = new WebHookEvent(bytes);
	
		if (isAuthenticated(event, request)) {
		

			eventBus.post(event);

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

	boolean isAuthenticated(WebHookEvent event, HttpServletRequest request) {
		
		if (authenticatorList.isEmpty()) {
			// if no authenticators are set up, assume we want to just trust everything
			return true;
		}
		
		for (WebHookAuthenticator auth: authenticatorList) {
		
			Optional<Boolean> b = auth.authenticate(event, request);
			if (b.isPresent()) {
				return b.get();
			}
		}
		return false;
	}

	public void addAuthenticator(WebHookAuthenticator auth) {
		Preconditions.checkNotNull(auth);
		authenticatorList.add(auth);
	}
}
