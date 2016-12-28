package io.macgyver.core.event;

import javax.annotation.PostConstruct;

import org.lendingclub.reflex.predicate.Predicates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.macgyver.core.util.JsonNodes;

public class Slf4jEventWriter {

	Logger logger = LoggerFactory.getLogger(Slf4jEventWriter.class);
	@Autowired
	EventSystem eventSystem;

	@PostConstruct
	public void subscribe() {
		eventSystem.getObservable().filter(Predicates.type(MacGyverMessage.class)).subscribe(x-> {
			if (logger.isDebugEnabled()) {
				logger.debug("logging event:\n {}",JsonNodes.pretty(((MacGyverMessage)x).getEnvelope()));
			}
		});
	}
}
