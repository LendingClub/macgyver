package io.macgyver.core.web.mvc;

import io.macgyver.neorx.rest.NeoRxClient;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import com.google.common.io.BaseEncoding;

@Component("macSnapShareController")
@Controller
@PreAuthorize("hasAnyRole('ROLE_MACGYVER_USER')")
public class SnapShareController {

	Logger logger = LoggerFactory.getLogger(SnapShareController.class);
	
	int expirationMinutes = 15;

	@Autowired
	NeoRxClient neo4j;

	public SnapShareController() {

	}

	@RequestMapping("/core/snap-share")
	@ResponseBody
	public ModelAndView search(HttpServletRequest request) {

		String secret = request.getParameter("secret");
		String token = request.getParameter("token");
		boolean notFound = false;

		purgeExpired();
		
		if (secret != null) {
			token = UUID.randomUUID().toString().replace("-", "");

			String val = encrypt(secret);
			
			long expiration = System.currentTimeMillis()+TimeUnit.MINUTES.toMillis(expirationMinutes);
			
			neo4j.execCypher(
					"merge (s:SnapShare {token: {token}}) set s.value={value}, s.updateTs=timestamp(), s.expirationTs={expirationTs}",
					"token", token, "value", val,"expirationTs",expiration);

			String url = request.getRequestURL() + "?token=" + token;

			Map<String, Object> m = Maps.newHashMap();
			m.put("tokenUrl", url);
			m.put("expirationMinutes", expirationMinutes);

			return new ModelAndView("/core/snap-share", m);
		}

		if (token != null) {
			try {

				JsonNode n = neo4j
						.execCypher(
								"match (s:SnapShare {token:{token}}) where timestamp()<s.expirationTs  return s",
								"token", token)
						.toBlocking().first();
			
				String v = decrypt(n.path("value").asText());

				
				return new ModelAndView("/core/snap-share", "secret", v);

			} catch (RuntimeException e) {
				logger.warn("retrieval failure: "+e.toString());
				notFound = true;
			}
		}

		return new ModelAndView("/core/snap-share", "notFound", true);

	}
	
	String encrypt(String input) {
		// no encryption for now, just base64 encoding
		return BaseEncoding.base64().encode(input.getBytes());
	}
	
	String decrypt(String input) {
		// no encryption for now, just base64 encoding
		return new String(BaseEncoding.base64().decode(input));
	}
	
	public void purgeExpired() {
		neo4j.execCypher("match (s:SnapShare) where timestamp()>s.expirationTs delete s");
	}

}
