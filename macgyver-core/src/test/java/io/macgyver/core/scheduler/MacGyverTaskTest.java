package io.macgyver.core.scheduler;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class MacGyverTaskTest {

	@Test
	public void testIt() {
		
		ObjectNode params = new ObjectMapper()
		.createObjectNode().put("foo", "bar").put("intval", 2)
		.put("boolval", true);
		
		params.set("objectNode", new ObjectMapper().createObjectNode());
		
		params.set("arrayNode", new ObjectMapper().createObjectNode());
		
		ObjectNode n = new ObjectMapper().createObjectNode();
		n.set("parameters", params);
		
		MacGyverTask task = new MacGyverTask(n);

		Map<String, Object> m = task.createArgsFromConfig();

		Assertions.assertThat(m).hasSize(3).containsEntry("foo", "bar")
				.containsEntry("intval", "2").containsEntry("boolval", "true");

	}
}