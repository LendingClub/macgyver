package io.macgyver.plugin.elb.a10;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class A10HAClientImplTest {

	@Test
	public void testActiveCheckInterval() throws IOException {

		Assertions.assertThat(A10HAClientImpl.DEFAULT_NODE_CHECK_SECS)
				.isEqualTo(60);

	}

}
