package io.macgyver.core.util;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Neo4jUtilTest {
	Logger logger = LoggerFactory.getLogger(Neo4jUtilTest.class);

	@Test
	public void testIt() {

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode n = mapper.createObjectNode();

		n.put("a", "1");
		n.put("b", (String) null);
		ArrayNode an = mapper.createArrayNode();
		an.add("a");
		an.add("b");
		an.add(mapper.createObjectNode());
		n.set("c", an);

		n.set("d", mapper.createObjectNode());

		n.set("e", MissingNode.getInstance());
		n.set("f", NullNode.getInstance());

		an = mapper.createArrayNode();
		an.add("foo");
		n.set("g", an);

		ObjectNode copy = Neo4jUtil.scrubNonCompliantNeo4jAttributes(n);

		logger.info("source: {}", n);
		logger.info("scrubbed: {}", copy);

		Assertions.assertThat(copy).isNotSameAs(n);
		Assertions.assertThat(copy.get("a").asText()).isEqualTo("1");

		Assertions.assertThat(copy.get("g").isArray()).isTrue();

		Assertions.assertThat(copy.get("a").asText()).isEqualTo("1");
		Assertions.assertThat(copy.get("b").isNull()).isTrue();
		Assertions.assertThat(copy.has("c")).isFalse();
		Assertions.assertThat(copy.has("d")).isFalse();
		Assertions.assertThat(copy.has("e")).isFalse();

	}
}
