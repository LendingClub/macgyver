package io.macgyver.discovery.agent;

import java.util.Properties;

import org.junit.Test;

public class MacGyverDiscoveryAgentTest {

	
	@Test
	public void testIt() throws InterruptedException {
		MacGyverDiscoveryAgent agent = new MacGyverDiscoveryAgent();
		
		Properties p = new Properties();
			p.put("macgyver.discovery.agent.sender.etcd", "foo");
			p.put("macgyver.discovery.agent.sender.etcd.bar", "foo2");
			p.put("macgyver.discovery.agent.sender.rest.url", "https://macgyver.example.com");
			p.put("macgyver.discovery.agent.sender.rest.interval", "1");
		MetadataCollector c = new MetadataCollector() {
			
			@Override
			public void collect(MetadataProperties p) {
				p.put("foo", "bar");
				p.put("xxx", "yyy");
				p.put("env","myenv");
				p.put("app", "myapp");
				p.put("host", "host");
			}
		};
		agent.addCollector(c);
		
		agent.init(p);
		
	
		
		Thread.sleep(10000);
		
		agent.shutdown();
		
	}
}
