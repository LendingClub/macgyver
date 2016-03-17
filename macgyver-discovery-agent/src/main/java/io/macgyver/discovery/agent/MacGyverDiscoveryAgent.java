package io.macgyver.discovery.agent;

import io.macgyver.discovery.agent.MetadataCollector.MetadataProperties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MacGyverDiscoveryAgent {

	java.util.logging.Logger log = java.util.logging.Logger
			.getLogger(MacGyverDiscoveryAgent.class.getName());

	String env;
	String app;
	String host;

	private List<MetadataCollector> collectorList = new CopyOnWriteArrayList<MetadataCollector>();
	private List<Sender> senderList = new CopyOnWriteArrayList<Sender>();

	Pattern PATTERN = Pattern
			.compile("(macgyver\\.discovery\\.agent\\.sender)\\.*(\\w*)\\.*(.*)");



	public MacGyverDiscoveryAgent addSender(Sender s) {
		s.agent = this;
		senderList.add(s);
		s.start();
		return this;
	}

	public MacGyverDiscoveryAgent addCollector(MetadataCollector d) {
		collectorList.add(d);
		return this;
	}

	public Properties collect() {
		Properties p = new Properties();
		MetadataProperties px = new MetadataCollector.MetadataProperties();

		px.properties = p;

		if (env != null) {
			p.put("env", env);
		}
		if (app != null) {
			p.put("app", env);
		}
		if (host != null) {
			p.put("instance", env);
		}

		for (MetadataCollector d : collectorList) {
			try {
				d.collect(px);
			} catch (RuntimeException e) {
				log.log(java.util.logging.Level.WARNING,
						"problem collecting metadata", e);
			}

		}

		return p;
	}

	public void send(Properties p) {
		for (Sender s : senderList) {
			try {
				log.info("sending " + s);
				s.send(p);
			} catch (Exception e) {
				log.log(java.util.logging.Level.WARNING,
						"problem sending metadata", e);
			}
		}
	}

	public void shutdown() {
		log.info("shutting down");
		for (Sender s : senderList) {
			try {
				log.info("shutting down " + s);
				s.stop();
			} catch (Exception e) {
				log.log(java.util.logging.Level.WARNING,
						"problem stopping sender", e);
			}
		}
	}

	public void configureSender(String name, Properties p) {
		try {
			log.info("configuring sender: " + name);
			Properties scoped = new Properties();
			for (Object k : p.keySet()) {
				Matcher m = PATTERN.matcher(k.toString());
				if (m.matches() && name.equals(m.group(2))) {
					String scopedProperty = m.group(3);
	
					if (scopedProperty.length() > 0) {
						scoped.put(scopedProperty, p.getProperty(k.toString()));
					}

				}
			}
			String className = getClass().getPackage().getName() + "."
					+ name.substring(0, 1).toUpperCase() + name.substring(1)
					+ "Sender";

			Sender sender = (Sender) Class.forName(className).newInstance();
			sender.configure(scoped);

			addSender(sender);
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException | RuntimeException e) {
			log.log(java.util.logging.Level.WARNING,
					"problem configuring sender", e);
		}

	}

	public void init(Properties props) {
		for (String name : getSenderNames(props)) {
			configureSender(name, props);
		}
	}

	public Collection<String> getSenderNames(Properties props) {
		Set<String> senders = new HashSet<>();

		for (Object k : props.keySet()) {
			Matcher m = PATTERN.matcher(k.toString());
			if (m.matches()) {
				senders.add(m.group(2));

			}
		}
		return senders;
	}
}
