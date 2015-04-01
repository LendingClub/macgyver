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

import io.macgyver.plugin.elb.ElbException;

import java.io.IOException;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.tomcat.util.net.URL;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.squareup.okhttp.ConnectionSpec;
import com.squareup.okhttp.ConnectionSpec.Builder;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.TlsVersion;

public class A10ClientImpl implements A10Client {

	public static final String A10_AUTH_TOKEN_KEY = "token";
	Logger logger = LoggerFactory.getLogger(A10ClientImpl.class);
	private String username;
	private String password;
	private String url;
	LoadingCache<String, String> tokenCache;

	public static final int DEFAULT_TOKEN_CACHE_DURATION = 10;
	private static final TimeUnit DEFAULT_TOKEN_CACHE_DURATION_TIME_UNIT = TimeUnit.MINUTES;

	ObjectMapper mapper = new ObjectMapper();
	public boolean validateCertificates = true;

	boolean immutable = false;

	public A10ClientImpl(String url, String username, String password) {
		this.url = normalizeUrl(url);
		this.username = username;
		this.password = password;

		setTokenCacheDuration(DEFAULT_TOKEN_CACHE_DURATION,
				DEFAULT_TOKEN_CACHE_DURATION_TIME_UNIT);
		logger.info("url: {}", this.url);

	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setTokenCacheDuration(int duration, TimeUnit timeUnit) {
		Preconditions.checkArgument(duration >= 0, "duration must be >=0");
		Preconditions.checkNotNull(timeUnit, "TimeUnit must be set");

		this.tokenCache = CacheBuilder.newBuilder()
				.expireAfterWrite(duration, timeUnit)
				.build(new TokenCacheLoader());

	}

	public void setCertificateVerificationEnabled(boolean b) {
		validateCertificates = b;
		if (validateCertificates && (!b)) {
			logger.warn("certificate validation disabled");
		}
	}

	void throwExceptionIfNecessary(Element element) {
		if (element.getName().equals("response")) {
			String status = element.getAttributeValue("status");

			if (status.equalsIgnoreCase("ok")) {
				// ok
			} else if (status.equalsIgnoreCase("fail")) {
				String code = "";
				String msg = "";

				Element error = element.getChild("error");
				if (error != null) {
					code = error.getAttributeValue("code");
					msg = error.getAttributeValue("msg");
				}

				throw new A10RemoteException(code, msg);
			} else {
				logger.warn("unexpected status: {}", status);
			}

		} else {
			logger.warn("unexpected response element: {}", element.getName());
		}

	}

	void throwExceptionIfNecessary(ObjectNode response) {

		if (response.has("response") && response.get("response").has("err")) {

			String code = response.path("response").path("err").path("code")
					.asText();
			String msg = response.path("response").path("err").path("msg")
					.asText();

			logger.warn("error response: {}", response);
			A10RemoteException x = new A10RemoteException(code, msg);
			throw x;

		}

	}

	/**
	 * This probably does not have a lot of practical value outside of testing.
	 * By forcibly setting the the authentication token, we can prevent an
	 * implicit call to authenticate(). This helps simplify mocked server
	 * exchanges.
	 * 
	 * @param token
	 */
	public void setAuthToken(String token) {
		tokenCache.put(A10_AUTH_TOKEN_KEY, token);
	}

	/**
	 * Performs an authentication, caches the resulting authentication token,
	 * and returns it.
	 * 
	 * @return
	 */
	public String authenticate() {

		try {
			FormEncodingBuilder b = new FormEncodingBuilder();
			b = b.add("username", username).add("password", password)
					.add("format", "json").add("method", "authenticate");
	
			
			Request r = new Request.Builder().url(getUrl())
					.addHeader("Accept", "application/json").post(b.build())
					.build();
			Response resp = getClient().newCall(r).execute();

			ObjectNode obj = parseJsonResponse(resp, "authenticate");

			String sid = obj.path("session_id").asText();
			if (Strings.isNullOrEmpty(sid)) {
				throw new ElbException("authentication failed");
			}
			tokenCache.put(A10_AUTH_TOKEN_KEY, sid);
			return sid;

		} catch (IOException e) {
			throw new ElbException(e);
		}

	}

	class TokenCacheLoader extends CacheLoader<String, String> {

		@Override
		public String load(String arg0) throws Exception {
			return authenticate();
		}

	}

	protected String getAuthToken() {
		try {
			return tokenCache.get(A10_AUTH_TOKEN_KEY);
		} catch (ExecutionException e) {
			throw new ElbException(e.getCause());
		}

	}

	protected Map<String, String> toMap(String... args) {
		Map<String, String> m = Maps.newHashMap();
		if (args == null || args.length == 0) {
			return m;
		}
		if (args.length % 2 != 0) {
			throw new IllegalArgumentException(
					"arguments must be in multiples of 2 (key/value)");
		}
		for (int i = 0; i < args.length; i += 2) {
			Preconditions.checkNotNull(args[i]);
			Preconditions.checkNotNull(args[i + 1]);
			m.put(args[i], args[i + 1]);
		}
		return m;
	}

	@Deprecated
	public ObjectNode invoke(String method, String... args) {
		return invokeJson(method, args);
	}

	@Override
	public ObjectNode invokeJson(String method,  String... args) {
		return invokeJson(method, null, toMap(args));
	}
	@Override
	public ObjectNode invokeJson(String method, JsonNode body, String... args) {
		return invokeJson(method, toMap(args));
	}

	@Override
	public Element invokeXml(String method, Element body, String... args) {
		return invokeXml(method, body, toMap(args));
	}
	@Override
	public Element invokeXml(String method, String... args) {
		return invokeXml(method, null, toMap(args));
	}

	@Deprecated
	public ObjectNode invoke(String method, Map<String, String> params) {
		return invokeJson(method, params);
	}

	public ObjectNode invokeJson(String method, Map<String, String> params) {
		return invokeJson(method,null,params);
	}
	public ObjectNode invokeJson(String method, JsonNode body, Map<String, String> params) {
		if (params == null) {
			params = Maps.newConcurrentMap();
		}
		Map<String, String> copy = Maps.newHashMap(params);
		copy.put("method", method);

		return invokeJson(copy, body);
	}

	@Override
	public Element invokeXml(String method, Map<String, String> params) {
		
		return invokeXml(method,null,params);
	}
	@Override
	public Element invokeXml(String method, Element body, Map<String, String> params) {
		if (params == null) {
			params = Maps.newConcurrentMap();
		}
		Map<String, String> copy = Maps.newHashMap(params);
		copy.put("method", method);

		return invokeXml(copy, body);
	}

	

	protected Element parseXmlResponse(Response response, String method) {
		try {
			Document d = new SAXBuilder().build(response.body().charStream());

			return d.getRootElement();
		} catch (IOException | JDOMException e) {
			throw new ElbException(e);
		}
	}

	protected ObjectNode parseJsonResponse(Response response, String method) {
		try {
			Preconditions.checkNotNull(response);

			String contentType = response.header("Content-type");

			

			// aXAPI is very sketchy with regard to content type of response.
			// Sometimes we get XML/HTML back even though
			// we ask for JSON. This hack helps figure out what is going on.

			String val = response.body().string().trim();
			if (!val.startsWith("{") && !val.startsWith("[")) {
				throw new ElbException("response contained non-JSON data: "
						+ val);
			}

			ObjectNode json = (ObjectNode) mapper.readTree(val);

			if (logger.isDebugEnabled()) {

				String body = mapper.writerWithDefaultPrettyPrinter()
						.writeValueAsString(json);
				logger.debug("response: \n{}", body);
			}
			throwExceptionIfNecessary(json);
			return json;
		} catch (IOException e) {
			throw new ElbException(e);
		}
	}

	protected ObjectNode invokeJson(Map<String, String> x, JsonNode optionalBody) {

		if (optionalBody!=null) {
			throw new UnsupportedOperationException("POST operations with JSON body not yet supported");
		}
		try {

			String method = x.get("method");
			Preconditions.checkArgument(!Strings.isNullOrEmpty(method),
					"method argument must be passed");

			FormEncodingBuilder fb = new FormEncodingBuilder()
					.add("session_id", getAuthToken()).add("format", "json");
			
			for (Map.Entry<String, String> entry: x.entrySet()) {
				fb = fb.add(entry.getKey(), entry.getValue());
			}

			Response resp = getClient().newCall(
					new Request.Builder().url(getUrl()).post(fb.build())
							.build()).execute();

			return parseJsonResponse(resp, method);

		} catch (IOException e) {
			throw new ElbException(e);
		}

	}

	protected Element invokeXml(Map<String, String> x, Element optionalBody) {

		if (optionalBody!=null) {
			throw new UnsupportedOperationException("POST operations with XML body not yet supported");
		}
		try {

			String method = x.get("method");
			Preconditions.checkArgument(!Strings.isNullOrEmpty(method),
					"method argument must be passed");

			FormEncodingBuilder fb = new FormEncodingBuilder()
					.add("session_id", getAuthToken()).add("format", "xml");
			for (Map.Entry<String, String> entry: x.entrySet()) {
				fb = fb.add(entry.getKey(), entry.getValue());
			}
			Response resp = getClient().newCall(
					new Request.Builder().url(getUrl()).post(fb.build())
							.build()).execute();

			Element element = parseXmlResponse(resp, method);
			throwExceptionIfNecessary(element);
			return element;

		} catch (IOException e) {
			throw new ElbException(e);
		}

	}

	

	AtomicReference<OkHttpClient> clientReference = new AtomicReference<OkHttpClient>();

	protected OkHttpClient getClient() {

		// not guaranteed to be singleton, but close enough
		if (clientReference.get() == null) {
			OkHttpClient c = new OkHttpClient();
			
			c.setConnectTimeout(20, TimeUnit.SECONDS);

			c.setHostnameVerifier(withoutHostnameVerification());
			c.setSslSocketFactory(withoutCertificateValidation()
					.getSocketFactory());

			c.setConnectionSpecs(getA10CompatibleConnectionSpecs());
			clientReference.set(c);
		}
		return clientReference.get();
	}

	public static HostnameVerifier withoutHostnameVerification() {
		HostnameVerifier verifier = new HostnameVerifier() {

			@Override
			public boolean verify(String hostname, SSLSession session) {
				// TODO Auto-generated method stub
				return true;
			}
		};
		return verifier;
	}

	static AtomicReference<SSLContext> trustAllContext = new AtomicReference<>();

	public static synchronized SSLContext withoutCertificateValidation() {
		try {
			SSLContext sslContext = trustAllContext.get();
			if (sslContext != null) {
				return sslContext;
			}
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

				@Override
				public X509Certificate[] getAcceptedIssuers() {

					return null;
				}

				@Override
				public void checkServerTrusted(X509Certificate[] arg0,
						String arg1) throws CertificateException {

				}

				@Override
				public void checkClientTrusted(X509Certificate[] arg0,
						String arg1) throws CertificateException {

				}
			} };

			// A10 management port seems to implement a fairly broken HTTPS
			// stack which
			// does not support re-negotiation
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, trustAllCerts,
					new java.security.SecureRandom());

			trustAllContext.set(sslContext);
			return sslContext;
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * The A10 control port has a brain-dead HTTPS stack that is unable to
	 * negotiate TLS versions.
	 * 
	 * @return
	 */
	List<ConnectionSpec> getA10CompatibleConnectionSpecs() {
		List<ConnectionSpec> list = Lists.newArrayList();

		list.add(new Builder(ConnectionSpec.MODERN_TLS).tlsVersions(
				TlsVersion.TLS_1_0).build()); // This is essential
		list.add(ConnectionSpec.MODERN_TLS);
		list.add(ConnectionSpec.CLEARTEXT);
		return ImmutableList.copyOf(list);
	}

	@Override
	public boolean isActive() {
		try {
			Element e = invokeXml("ha.group.fetchStatistics");

			Element statusListElement = e.getChild("ha_group_status_list");
			if (statusListElement == null
					|| statusListElement.getChildren().isEmpty()) {
				return true;
			}
			String x = e.getChild("ha_group_status_list").getChildren().get(0)
					.getChildTextTrim("local_status");
			return Strings.nullToEmpty(x).equals("1");

		} catch (ElbException e) {
			throw e;
		} catch (RuntimeException e) {
			throw new ElbException(e);
		}
	}

	public String toString() {
		return MoreObjects.toStringHelper(this).add("url", url).toString();
	}

	protected String normalizeUrl(String url) {

		Preconditions.checkNotNull(url);
		Preconditions.checkArgument(
				url.startsWith("http://") || url.startsWith("https://"),
				"url must be http(s)");
		try {

			URL u = new URL(url);

			String normalized = u.getProtocol() + "://" + u.getHost()
					+ ((u.getPort() > 0) ? ":" + u.getPort() : "")
					+ "/services/rest/v2/";

			return normalized;
		} catch (IOException e) {
			throw new IllegalArgumentException("invalid url: " + url);
		}
	}

}
