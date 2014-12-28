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

import io.macgyver.core.ServiceInvocationException;
import io.macgyver.core.jaxrs.SslTrust;
import io.macgyver.plugin.elb.ElbException;

import java.io.IOException;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.squareup.okhttp.ConnectionSpec;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.TlsVersion;
import com.squareup.okhttp.ConnectionSpec.Builder;

public class A10ClientImpl implements A10Client {

	public static final String A10_AUTH_TOKEN_KEY = "token";
	Logger logger = LoggerFactory.getLogger(A10ClientImpl.class);
	private String username;
	private String password;
	private String url;
	Cache<String, String> tokenCache;

	public static final int DEFAULT_TOKEN_CACHE_DURATION = 10;
	private static final TimeUnit DEFAULT_TOKEN_CACHE_DURATION_TIME_UNIT = TimeUnit.MINUTES;

	ObjectMapper mapper = new ObjectMapper();
	public boolean validateCertificates = true;

	boolean immutable = false;

	public A10ClientImpl(String url, String username, String password) {
		this.url = url;
		this.username = username;
		this.password = password;

		setTokenCacheDuration(DEFAULT_TOKEN_CACHE_DURATION,
				DEFAULT_TOKEN_CACHE_DURATION_TIME_UNIT);

	}

	protected List<String> getMethodsWithBrokenJson() {
		return ImmutableList.of("system.performance.get",
				"system.information.get", "system.device_info.get");
	}

	public void setUsername(String username) {
		checkMutable();
		this.username = username;
	}

	public void setPassword(String password) {
		checkMutable();
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		checkMutable();
		this.url = url;
	}

	public void markImmutable() {
		immutable = true;
	}

	protected void checkMutable() {
		if (immutable) {
			throw new IllegalStateException("object is immutable");
		}
	}

	public void setTokenCacheDuration(int duration, TimeUnit timeUnit) {
		Preconditions.checkArgument(duration >= 0, "duration must be >=0");
		Preconditions.checkNotNull(timeUnit, "TimeUnit must be set");

		this.tokenCache = CacheBuilder.newBuilder()
				.expireAfterWrite(duration, timeUnit).build();

	}

	public void setCertificateVerificationEnabled(boolean b) {
		checkMutable();
		validateCertificates = b;
		if (validateCertificates && (!b)) {
			logger.warn("certificate validation disabled");
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

	protected String authenticate() {

		try {
			FormEncodingBuilder b = new FormEncodingBuilder();
			b = b.add("username", username).add("password", password)
					.add("format", "json").add("method", "authenticate");

			Request r = new Request.Builder().url(getUrl()).post(b.build())
					.build();
			Response resp = getClient().newCall(r).execute();

			ObjectNode obj = parseResponse(resp, "authenticate");

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

	protected String getAuthToken() {
		String token = tokenCache.getIfPresent(A10_AUTH_TOKEN_KEY);
		if (token == null) {
			token = authenticate();
		}

		if (token == null) {
			throw new ElbException("could not obtain auth token");
		}
		return token;

	}

	public ObjectNode invoke(String method) {
		Map<String, String> m = Maps.newHashMap();
		return invoke(method, m);
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

	@Override
	public ObjectNode invoke(String method, String... args) {
		return invoke(method, toMap(args));
	}

	@Override
	public ObjectNode invoke(String method, Map<String, String> params) {
		if (params == null) {
			params = Maps.newConcurrentMap();
		}
		Map<String, String> copy = Maps.newHashMap(params);
		copy.put("method", method);

		return invoke(copy);
	}

	protected Optional<ObjectNode> patchBrokenResponse(Response response,
			String method) {
		if (method == null) {
			return Optional.absent();
		}
		try {
			// The A10 lies about the content of the response in some cases. It
			// will
			// set JSON content-type header
			// but return an XML payload. We try to patch this up here.
			if (getMethodsWithBrokenJson().contains(method)) {

				String bodyContent = response.body().string();
				Document doc = DocumentBuilderFactory
						.newInstance()
						.newDocumentBuilder()
						.parse(new org.xml.sax.InputSource(new StringReader(
								bodyContent)));
				Node n = doc.getDocumentElement().getFirstChild();
				NodeList nl = n.getChildNodes();

				ObjectNode top = mapper.createObjectNode();
				ObjectNode x = mapper.createObjectNode();
				top.set(n.getNodeName(), x);

				for (int i = 0; i < nl.getLength(); i++) {
					Node item = nl.item(i);
					String val = item.getTextContent();
					try {
						long longVal = Long.parseLong(val);
						x.put(item.getNodeName(), longVal);
					} catch (Exception e) {
						x.put(item.getNodeName(), val);
					}

				}

				return Optional.of(top);

			}

			else {
				return Optional.absent();
			}
		} catch (ParserConfigurationException | IOException | SAXException e) {
			throw new ElbException(e);
		}
	}

	protected ObjectNode parseResponse(Response response, String method) {
		try {
			Preconditions.checkNotNull(response);

			String contentType = response.header("Content-type");

			Optional<ObjectNode> patchedResponse = patchBrokenResponse(
					response, method);
			if (patchedResponse.isPresent()) {
				return patchedResponse.get();
			}
			ObjectNode json = (ObjectNode) mapper.readTree(response.body()
					.charStream());

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

	protected ObjectNode invoke(Map<String, String> x) {

		try {

			String method = x.get("method");
			Preconditions.checkArgument(!Strings.isNullOrEmpty(method),
					"method argument must be passed");

			FormEncodingBuilder fb = new FormEncodingBuilder()
					.add("session_id", getAuthToken()).add("format", "json")
					.add("method", method);

			Response resp = getClient().newCall(
					new Request.Builder().url(getUrl()).post(fb.build())
							.build()).execute();

			return parseResponse(resp, method);

		} catch (IOException e) {
			throw new ElbException(e);
		}

	}

	@Override
	public ObjectNode getDeviceInfo() {
		return invoke("system.device_info.get");
	}

	@Override
	public ObjectNode getSystemInfo() {
		return invoke("system.information.get");
	}

	@Override
	public ObjectNode getSystemPerformance() {
		return invoke("system.performance.get");
	}

	@Override
	public ObjectNode getServiceGroupAll() {

		ObjectNode obj = invoke("slb.service_group.getAll");

		return obj;

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

		return ImmutableList.copyOf(list);
	}
}
