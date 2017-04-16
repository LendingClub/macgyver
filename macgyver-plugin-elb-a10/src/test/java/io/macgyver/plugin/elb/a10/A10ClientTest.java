/**
 * Copyright 2017 Lending Club, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.macgyver.plugin.elb.a10;

import static org.assertj.core.api.Assertions.assertThat;
import io.macgyver.core.LoggingConfig;
import io.macgyver.test.RequestUtil;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import com.squareup.okhttp.mockwebserver.MockWebServer;

public class A10ClientTest {

	ObjectMapper mapper = new ObjectMapper();
	@Rule
	public MockWebServer mockServer = new MockWebServer();

	public A10ClientImpl testClient = null;

	@BeforeClass
	public static void bridgeLogging() {
		LoggingConfig.ensureJavaUtilLoggingIsBridged();
	}

	@Before
	public void setupTestClient() {

		// Do not call it a mock client. It is a real client and a mock server!

		// instantiate a test client that will communicate with our mock server
		testClient = new A10ClientImpl(mockServer.getUrl("/services/rest/v2/")
				.toString(), "dummyuser", "dummypassword");

		// set the auth token to prevent the first call from being an
		// authenticate request
		testClient.setAuthToken("prevent_implicit_authenticate_call");
	}

	@Test
	public void testInvalidSession() throws IOException {
		String invalidMethod = "{\n" + "  \"response\" : {\n"
				+ "    \"status\" : \"fail\",\n" + "    \"err\" : {\n"
				+ "      \"code\" : 1009,\n"
				+ "      \"msg\" : \"Invalid session ID\"\n" + "    }\n"
				+ "  }\n" + "}";

		mockServer.enqueue(new MockResponse().setBody(invalidMethod));

		try {
			testClient.invokeJson("foo");
			Assert.fail("exception not thrown");
		} catch (A10RemoteException e) {

			Assert.assertEquals("1009", e.getErrorCode());
			Assert.assertEquals("Invalid session ID", e.getErrorMessage());
		}
	}

	@Test
	public void testRemoteException() throws IOException {
		String json = "{\n" + "  \"response\" : {\n"
				+ "    \"status\" : \"fail\",\n" + "    \"err\" : {\n"
				+ "      \"code\" : 1008,\n"
				+ "      \"msg\" : \"Invalid web service method name\"\n"
				+ "    }\n" + "  }\n" + "}";

		mockServer.enqueue(new MockResponse().setBody(json));

		try {
			testClient.invokeJson("foo");

			Assert.fail("exception not thrown");
		} catch (A10RemoteException e) {
			Assert.assertEquals("1008", e.getErrorCode());
			Assert.assertEquals("Invalid web service method name",
					e.getErrorMessage());
		}
	}

	@Test
	public void testToMap() {
		A10ClientImpl client = new A10ClientImpl("http://localhost", "xx", "");
		Assert.assertTrue(client.toMap(null).isEmpty());
		Assert.assertEquals("2", client.toMap("a", "1", "b", "2").get("b"));

	}

	@Test
	public void testThrowExceptionIfNecessaryJson() throws IOException,
			JDOMException {

		try {

			JsonNode x = mapper
					.createObjectNode()
					.set("response",
							mapper.createObjectNode()
									.set("err",
											mapper.createObjectNode()
													.put("code", "1008")
													.put("msg",
															"Invalid web service method name")));

			mockServer.enqueue(new MockResponse().setBody(mapper
					.writeValueAsString(x)));

			testClient.invokeJson("foo");

			Assert.fail();
		} catch (A10RemoteException e) {
			Assertions.assertThat(e).hasMessageContaining("1008")
					.hasMessageContaining("Invalid web service method name");
		}

	}

	@Test
	public void testThrowExceptionIfNecessaryXml() throws IOException,
			JDOMException {

		try {
			String failure = "<response status=\"fail\">\n"
					+ "  <error code=\"1008\" msg=\"Invalid web service method name\" />\n"
					+ "</response>";

			mockServer.enqueue(new MockResponse().setBody(failure));

			testClient.invokeXml("foo");

			Assert.fail();
		} catch (A10RemoteException e) {
			Assertions.assertThat(e).hasMessageContaining("1008")
					.hasMessageContaining("Invalid web service method name");
		}

	}

	@Test
	public void okStatusShouldNotThrowException() throws IOException,
			JDOMException {

		mockServer.enqueue(new MockResponse()
				.setBody("<response status=\"ok\"></response>"));

		testClient.invokeXml("foo");

	}

	@Test
	public void testAuthenticate() throws InterruptedException {
		mockServer.enqueue(new MockResponse()
				.setBody("{\"session_id\":\"xxx\"}"));
		String x = testClient.authenticate();

		Assertions.assertThat(x).isEqualTo("xxx");

		RecordedRequest rr = mockServer.takeRequest();

		Map<String, String> m = RequestUtil.parseFormBody(rr.getUtf8Body());

		Assertions.assertThat(m).containsEntry("format", "json")
				.containsEntry("method", "authenticate")
				.containsEntry("username", "dummyuser")
				.containsEntry("password", "dummypassword");

	}

	@Test
	public void testInvokeJsonWithBody() throws InterruptedException {
		mockServer.enqueue(new MockResponse()
				.setBody("{\"response\": {\"status\":\"ok\"}}"));

		JsonNode json = new ObjectMapper().createObjectNode().put("def", "456")
				.put("hello", "world");

		testClient.invokeJson("testmethod", json, "abc", "123", "foo", "bar");

		RecordedRequest rr = mockServer.takeRequest();

		String path = rr.getPath();
		Assertions.assertThat(path).contains("format=json").contains("abc=123")
				.contains("foo=bar").contains("method=testmethod")
				.contains("session_id");

		String requestBody = rr.getUtf8Body();
		Assertions.assertThat(requestBody).isEqualTo(json.toString());
	}

	@Test
	public void testInvokeJsonNoBody() throws InterruptedException {
		mockServer.enqueue(new MockResponse()
				.setBody("{\"response\": {\"status\":\"ok\"}}"));
		testClient.invokeJson("testmethod", "abc", "123", "foo", "bar");

		RecordedRequest rr = mockServer.takeRequest();

		Map<String, String> m = RequestUtil.parseFormBody(rr);

		Assertions.assertThat(m).containsEntry("abc", "123")
				.containsEntry("foo", "bar")
				.containsEntry("method", "testmethod")
				.containsEntry("format", "json").containsKey("session_id");
	}

	public Element createXml() throws IOException {
		Element xml = new Element("test");
		xml = xml.setAttribute("def", "456").setAttribute("hello", "world")
				.setText("testXml");
		Element firstNumber = new Element("number");
		Element secondNumber = new Element("number");
		firstNumber.setText("3");
		secondNumber.setText("5");
		xml.addContent(firstNumber);
		xml.addContent(secondNumber);

		return xml;

	}

	@Test
	public void testInvalidWebService() {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><response status=\"fail\"><error code=\"1008\" msg=\"Invalid web service method name\" /></response>";

		try {
			mockServer.enqueue(new MockResponse().setBody(xml));
			testClient.newRequest("foo").executeXml();
			Assert.fail();
		} catch (Exception e) {
			Assertions.assertThat(e).isInstanceOf(A10RemoteException.class)
					.hasMessageContaining("1008")
					.hasMessageContaining("Invalid web service");
		}

	}

	@Test
	public void testInvalidWebServiceJson() {
		String json = "{\"response\": {\"status\": \"fail\", \"err\": {\"code\": 1008, \"msg\": \"Invalid web service method name\"}}}";
		mockServer.enqueue(new MockResponse().setBody(json));

		try {
			testClient.newRequest("foo").executeJson();
			Assert.fail();
		} catch (Exception e) {
			Assertions.assertThat(e).isInstanceOf(A10RemoteException.class)
					.hasMessageContaining("1008")
					.hasMessageContaining("Invalid web service");
		}

	}

	@Test
	public void testInvokeXmlWithBody() throws InterruptedException,
			IOException {
		mockServer.enqueue(new MockResponse()
				.setBody("<response status=\"ok\"><dummy/></response>"));

		Element xml = createXml();

		testClient.newRequest("testmethod").body( xml).params( "abc", "123", "foo", "bar").executeXml();

		RecordedRequest rr = mockServer.takeRequest();

		String path = rr.getPath();
		Assertions.assertThat(path).contains("format=xml").contains("abc=123")
				.contains("foo=bar").contains("method=testmethod")
				.contains("session_id");

		String requestBody = rr.getBody().readUtf8();
		Assertions.assertThat(requestBody).isEqualTo(
				new XMLOutputter(Format.getRawFormat()).outputString(xml));

	}
	
	@Test
	public void deprecatedTestInvokeXmlWithBody() throws InterruptedException,
			IOException {
		mockServer.enqueue(new MockResponse()
				.setBody("<response status=\"ok\"><dummy/></response>"));

		Element xml = createXml();

		testClient.invokeXml("testmethod", xml, "abc", "123", "foo", "bar");

		RecordedRequest rr = mockServer.takeRequest();

		String path = rr.getPath();
		Assertions.assertThat(path).contains("format=xml").contains("abc=123")
				.contains("foo=bar").contains("method=testmethod")
				.contains("session_id");

		String requestBody = rr.getBody().readUtf8();
		Assertions.assertThat(requestBody).isEqualTo(
				new XMLOutputter(Format.getRawFormat()).outputString(xml));

	}

	@Test
	public void testInvokeXmlNoBody() throws InterruptedException {
		mockServer.enqueue(new MockResponse()
				.setBody("<response status=\"ok\"><dummy/></response>"));
		testClient.newRequest("testmethod").params( "abc", "123", "foo", "bar").executeXml();

		RecordedRequest rr = mockServer.takeRequest();

		Map<String, String> m = RequestUtil.parseFormBody(rr);

		Assertions.assertThat(m).containsEntry("abc", "123")
				.containsEntry("foo", "bar")
				.containsEntry("method", "testmethod")
				.containsEntry("format", "xml").containsKey("session_id");
	}
	@Test
	public void deprecatedTestInvokeXmlNoBody() throws InterruptedException {
		mockServer.enqueue(new MockResponse()
				.setBody("<response status=\"ok\"><dummy/></response>"));
		testClient.invokeXml("testmethod", "abc", "123", "foo", "bar");

		RecordedRequest rr = mockServer.takeRequest();

		Map<String, String> m = RequestUtil.parseFormBody(rr);

		Assertions.assertThat(m).containsEntry("abc", "123")
				.containsEntry("foo", "bar")
				.containsEntry("method", "testmethod")
				.containsEntry("format", "xml").containsKey("session_id");
	}

	@Test
	public void serverNotConfiguredForHAShouldBeActive()
			throws InterruptedException {

		mockServer
				.enqueue(new MockResponse()
						.setBody("<response status=\"ok\"><ha_group_status_list/></response>"));

		Assertions.assertThat(testClient.isActive()).isTrue();

	}

	@Test
	public void testActiveStatus() throws InterruptedException {

		mockServer.enqueue(new MockResponse()
				.setBody("<response status=\"ok\">\n"
						+ "  <ha_group_status_list>\n"
						+ "    <ha_group_status>\n" + "      <id>1</id>\n"
						+ "      <local_status>0</local_status>\n"
						+ "      <local_priority>100</local_priority>\n"
						+ "      <peer_status>0</peer_status>\n"
						+ "      <peer_priority>50</peer_priority>\n"
						+ "      <force_self_standby>0</force_self_standby>\n"
						+ "    </ha_group_status>\n"
						+ "  </ha_group_status_list>\n" + "</response>"));
		Assertions.assertThat(testClient.isActive()).isFalse();

		mockServer.enqueue(new MockResponse()
				.setBody("<response status=\"ok\">\n"
						+ "  <ha_group_status_list>\n"
						+ "    <ha_group_status>\n" + "      <id>1</id>\n"
						+ "      <local_status>1</local_status>\n"
						+ "      <local_priority>100</local_priority>\n"
						+ "      <peer_status>0</peer_status>\n"
						+ "      <peer_priority>50</peer_priority>\n"
						+ "      <force_self_standby>0</force_self_standby>\n"
						+ "    </ha_group_status>\n"
						+ "  </ha_group_status_list>\n" + "</response>"));
		Assertions.assertThat(testClient.isActive()).isTrue();
	}

	@Test
	public void testAuthenticateFailure() {
		try {
			mockServer
					.enqueue(new MockResponse()
							.setBody("{\"response\":{\"status\":\"fail\",\"err\":{\"code\":520486915,\"msg\":\"Admin password error\"}}}"));
			testClient.authenticate();
			Assert.fail();
		} catch (A10RemoteException e) {
			assertThat(e.getErrorCode()).isEqualTo("520486915");
			assertThat(e.getErrorMessage()).isEqualTo("Admin password error");
			assertThat(e.getMessage()).contains(
					"520486915: Admin password error");
		}
	}

	@Test
	public void testSystemInformationGet() {
		// system.information.get
		String response = "<?xml version=\"1.0\"?>\n"
				+ "<response status=\"ok\">\n"
				+ "	<system_information>\n"
				+ "		<serial_number>AX25000000000000</serial_number>\n"
				+ "		<current_time>02:26:00 PDT Tue Mar 05 2014</current_time>\n"
				+ "		<startup_mode>hard disk secondary</startup_mode>\n"
				+ "		<software_version>2.7</software_version>\n"
				+ "		<advanced_core_os_on_harddisk1>2.7</advanced_core_os_on_harddisk1>\n"
				+ "		<advanced_core_os_on_harddisk2>2.7</advanced_core_os_on_harddisk2>\n"
				+ "		<advanced_core_os_on_compact_flash1>2.6</advanced_core_os_on_compact_flash1>\n"
				+ "		<advanced_core_os_on_compact_flash2>2.6</advanced_core_os_on_compact_flash2>\n"
				+ "		<firmware_version>N/A</firmware_version>\n"
				+ "		<aflex_engine_version>2.0.0</aflex_engine_version>\n"
				+ "		<axapi_version>2.1</axapi_version>\n"
				+ "		<last_config_saved>22:04:13 PDT Fri Mar 03 2014</last_config_saved>\n"
				+ "		<technical_support>www.a10networks.com/support</technical_support>\n"
				+ "	</system_information>\n" + "</response>";
		mockServer.enqueue(new MockResponse().setBody(response));
	}

	@Test
	public void testGetDeviceInfo() {
		// "system.device_info.get"
		String response = "<?xml version=\"1.0\"?>\n"
				+ "<response status=\"ok\">\n" + "	<device_information>\n"
				+ "		<cpu_count>6</cpu_count>\n"
				+ "		<cpu_status>ALL_OK</cpu_status>\n"
				+ "		<cpu_temperature>29C/84F</cpu_temperature>\n"
				+ "		<disk_status>\n" + "			<disk1>active</disk1>\n"
				+ "			<disk2>unknow</disk2>\n" + "		</disk_status>\n"
				+ "		<disk_usage>37194972KB/78148192KB</disk_usage>\n"
				+ "		<fan_status>\n" + "			<Fan1>10593</Fan1>\n"
				+ "			<Fan2>7704</Fan2>\n" + "			<Fan3>10593</Fan3>\n"
				+ "			<Fan4>7704</Fan4>\n" + "			<Fan5>10593</Fan5>\n"
				+ "			<Fan6>7704</Fan6>\n" + "			<Fan7>9416</Fan7>\n"
				+ "			<Fan8>7704</Fan8>\n" + "		</fan_status>\n"
				+ "		<power_supply>\n" + "			<supply1>on</supply1>\n"
				+ "			<supply2>on</supply2>\n" + "		</power_supply>\n"
				+ "		<memory_usage>4852304KB/6122580KB</memory_usage>\n"
				+ "	</device_information>\n" + "</response>";
		mockServer.enqueue(new MockResponse().setBody(response));
	}

	@Test
	public void testSystemPerformance() throws InterruptedException {
		// "system.performance.get"

		String response = "<?xml version=\"1.0\"?>\n"
				+ "<response status=\"ok\">\n"
				+ "	<performance>\n"
				+ "		<total_throughput_bits_per_sec>158482952</total_throughput_bits_per_sec>\n"
				+ "		<l4_conns_per_sec>0</l4_conns_per_sec>\n"
				+ "		<l7_conns_per_sec>229</l7_conns_per_sec>\n"
				+ "		<l7_trans_per_sec>273</l7_trans_per_sec>\n"
				+ "		<ssl_conns_per_sec>22</ssl_conns_per_sec>\n"
				+ "		<ip_nat_conns_per_sec>0</ip_nat_conns_per_sec>\n"
				+ "		<total_new_conns_per_sec>251</total_new_conns_per_sec>\n"
				+ "		<total_current_conns>3882</total_current_conns>\n"
				+ "	</performance>\n"
				+ "	<attack_prevention>\n"
				+ "		<total_tcp_syn_received>189629575</total_tcp_syn_received>\n"
				+ "		<Total_syn_cookie_failures>19222</Total_syn_cookie_failures>\n"
				+ "	</attack_prevention>\n"
				+ "	<health_check_summary>\n"
				+ "		<servers>\n"
				+ "			<server_up_num>324</server_up_num>\n"
				+ "			<server_down_num>9</server_down_num>\n"
				+ "		</servers>\n"
				+ "		<ports>\n"
				+ "			<port_up_num>324</port_up_num>\n"
				+ "			<port_down_num>37</port_down_num>\n"
				+ "		</ports>\n"
				+ "	</health_check_summary>\n"
				+ "	<http_proxy>\n"
				+ "		<total_conns>188944799</total_conns>\n"
				+ "		<crrent_conns>3882</crrent_conns>\n"
				+ "		<server_conns_made>239331598</server_conns_made>\n"
				+ "	</http_proxy>\n"
				+ "	<conn_reuse>\n"
				+ "		<open_persistent_conns>0</open_persistent_conns>\n"
				+ "		<active_persisten_conns>0</active_persisten_conns>\n"
				+ "	</conn_reuse>\n"
				+ "	<compression>\n"
				+ "		<in_data_rate_bytes_per_sec>0</in_data_rate_bytes_per_sec>\n"
				+ "		<out_dat_rate_bytes_per_sec>0</out_dat_rate_bytes_per_sec>\n"
				+ "		<bandwidth_savings>0</bandwidth_savings>\n"
				+ "	</compression>\n" + "	<caching>\n"
				+ "		<hit_ratio>0</hit_ratio>\n"
				+ "		<total_requests>0</total_requests>\n"
				+ "		<cached_objects>0</cached_objects>\n" + "	</caching>\n"
				+ "</response>";
		mockServer.enqueue(new MockResponse().setBody(response));

		Element n = testClient.newRequest("system.performance.get").executeXml();

		RecordedRequest rr = mockServer.takeRequest();

		Map<String, String> m = RequestUtil.parseFormBody(rr);

	}



	@Test
	public void testFormatUrl() throws UnsupportedEncodingException {
		Map<String,String> m = ImmutableMap.of("format","json","foo","bar baz","nl","a\n1\tb\r\2");
		String url = testClient.formatUrl(m, "json");
		
		
		// we want to see that we have exactly 1 "format=json" key value pair in the URL
		
		String kv = "format=json";
		int idx = url.indexOf(kv);
		Assertions.assertThat(idx).isGreaterThan(0);
		
		Assertions.assertThat(url.substring(idx+kv.length())).doesNotContain(kv);
		
		
		Assertions.assertThat(url).doesNotContain("bar baz").contains("foo=bar+baz").contains("nl=a%0A1%09b%0D%02");
		
		
	
	}
}
