package io.macgyver.discovery.agent;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class MetadataCollectorTest {

	@Test
	public void testIt() {
		MetadataCollector.MetadataProperties mp = new MetadataCollector.MetadataProperties();

		mp = mp.appId("test").host("myhost").env("myenv").put("foo", "bar");

		Assertions.assertThat(mp.getProperties())
				.containsEntry("appId", "test").containsEntry("host", "myhost")
				.containsEntry("env", "myenv").containsEntry("foo", "bar");
	}
}
