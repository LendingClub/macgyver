package io.macgyver.discovery.agent;

import java.util.Properties;

import org.junit.Test;

public class EtcdSenderTest {

	
	@Test
	public void testIt() {
		EtcdSender sender = new EtcdSender();
		
		Properties p = new Properties();
		p.put("foo", "bar");
		p.put("env", "myenv");
		p.put("app", "myapp");
		p.put("host","myinstance");
		
		sender.send(p);
	}
	
	
	
}
