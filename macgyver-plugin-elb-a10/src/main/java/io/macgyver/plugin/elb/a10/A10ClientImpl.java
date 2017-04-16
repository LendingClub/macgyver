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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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

import org.apache.tomcat.util.net.URL;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.macgyver.core.jaxrs.SslTrust;
import io.macgyver.plugin.elb.ElbException;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class A10ClientImpl implements A10Client {

	public static final String A10_AUTH_TOKEN_KEY = "token";
	Logger logger = LoggerFactory.getLogger(A10ClientImpl.class);
	private String username;
	private String password;
	private String url;
	LoadingCache<String, String> tokenCache;

	public static final int DEFAULT_TOKEN_CACHE_DURATION = 5;
	private static final TimeUnit DEFAULT_TOKEN_CACHE_DURATION_TIME_UNIT = TimeUnit.MINUTES;

	public static final String UTF8 = "UTF-8";

	public static final String INVALID_SESSION_ID_CODE = "1009";
	ObjectMapper mapper = new ObjectMapper();
	public boolean validateCertificates = true;

	boolean immutable = false;

	public A10ClientImpl(String url, String username, String password) {
		this.url = normalizeUrl(url);
		this.username = username;
		this.password = password;

		setTokenCacheDuration(DEFAULT_TOKEN_CACHE_DURATION, DEFAULT_TOKEN_CACHE_DURATION_TIME_UNIT);
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

		this.tokenCache = CacheBuilder.newBuilder().expireAfterWrite(duration, timeUnit).build(new TokenCacheLoader());

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
				if (code != null && INVALID_SESSION_ID_CODE.equals(code)) {
					tokenCache.invalidateAll();
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

			String code = response.path("response").path("err").path("code").asText();
			String msg = response.path("response").path("err").path("msg").asText();

			logger.warn("error response: {}", response);
			if (code != null && INVALID_SESSION_ID_CODE.equals(code)) {
				tokenCache.invalidateAll();
			}
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
			FormBody.Builder b = new FormBody.Builder();
			b = b.add("username", username).add("password", password).add("format", "json").add("method",
					"authenticate");

			okhttp3.Request r = new okhttp3.Request.Builder().url(getUrl()).addHeader("Accept", "application/json")
					.post(b.build()).build();
			okhttp3.Response resp = getClient().newCall(r).execute();

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

	protected static Map<String, String> toMap(String... args) {
		Map<String, String> m = Maps.newHashMap();
		if (args == null || args.length == 0) {
			return m;
		}
		if (args.length % 2 != 0) {
			throw new IllegalArgumentException("arguments must be in multiples of 2 (key/value)");
		}
		for (int i = 0; i < args.length; i += 2) {
			Preconditions.checkNotNull(args[i]);
			Preconditions.checkNotNull(args[i + 1]);
			m.put(args[i], args[i + 1]);
		}
		return m;
	}

	@Override
	@Deprecated
	public ObjectNode invoke(String method, String... args) {
		return invokeJson(method, args);
	}

	@Override
	@Deprecated
	public ObjectNode invokeJson(String method, String... args) {
		return invokeJson(method, null, toMap(args));
	}

	@Override
	@Deprecated
	public ObjectNode invokeJson(String method, JsonNode body, String... args) {
		return invokeJson(method, body, toMap(args));
	}

	@Override
	@Deprecated
	public Response invokeJsonWithRawResponse(String method, JsonNode body, String... args) {
		return newRequest(method).body(body).params(args).execute();
	}

	@Override
	@Deprecated
	public Element invokeXml(String method, Element body, String... args) {
		return invokeXml(method, body, toMap(args));
	}

	@Override
	@Deprecated
	public Element invokeXml(String method, String... args) {
		return newRequest(method).params(args).executeXml();

	}

	@Override
	@Deprecated
	public Response invokeXmlWithRawResponse(String method, Element body, String... args) {

		return newRequest(method).body(body).params(args).execute();

	}

	@Override
	@Deprecated
	public ObjectNode invoke(String method, Map<String, String> params) {
		return newRequest(method).params(params).executeJson();

	}

	@Override
	@Deprecated
	public ObjectNode invokeJson(String method, Map<String, String> params) {

		return newRequest(method).params(params).executeJson();

	}

	@Override
	@Deprecated
	public ObjectNode invokeJson(String method, JsonNode body, Map<String, String> params) {

		return newRequest(method).body(body).params(params).executeJson();

	}

	@Override
	@Deprecated
	public Response invokeJsonWithRawResponse(String method, JsonNode body, Map<String, String> params) {
		return newRequest(method).body(body).params(params).execute();
	}

	@Override
	@Deprecated
	public Element invokeXml(String method, Map<String, String> params) {

		return newRequest(method).params(params).executeXml();

	}

	@Override
	@Deprecated
	public Element invokeXml(String method, Element body, Map<String, String> params) {
		return newRequest(method).body(body).params(params).executeXml();
	}

	@Override
	@Deprecated
	public Response invokeXmlWithRawResponse(String method, Element body, Map<String, String> params) {
		return newRequest(method).body(body).params(params).execute();
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
				throw new ElbException("response contained non-JSON data: " + val);
			}

			ObjectNode json = (ObjectNode) mapper.readTree(val);

			if (logger.isDebugEnabled()) {

				String body = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
				logger.debug("response: \n{}", body);
			}
			throwExceptionIfNecessary(json);
			return json;
		} catch (IOException e) {
			throw new ElbException(e);
		}
	}

	AtomicReference<OkHttpClient> clientReference = new AtomicReference<OkHttpClient>();

	protected OkHttpClient getClient() {

		
		// not guaranteed to be singleton, but close enough
		if (clientReference.get() == null) {
			OkHttpClient.Builder cb = new OkHttpClient.Builder()
					.connectTimeout(20, TimeUnit.SECONDS)
					.hostnameVerifier(withoutHostnameVerification())
					.sslSocketFactory(withoutCertificateValidation().getSocketFactory(),new TrustAllManager())
					.connectionSpecs(getA10CompatibleConnectionSpecs());
			// c.interceptors().add(LoggingInterceptor.create(A10ClientImpl.class));
			clientReference.set(cb.build());
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

	 static class TrustAllManager implements X509TrustManager {

			@Override
			public X509Certificate[] getAcceptedIssuers() {

				return new X509Certificate[0];
			}

			@Override
			public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

			}

			@Override
			public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

			}
		}
	public static synchronized SSLContext withoutCertificateValidation() {
		try {
			SSLContext sslContext = trustAllContext.get();
			if (sslContext != null) {
				return sslContext;
			}
			TrustManager[] trustAllCerts = new TrustManager[] { new TrustAllManager() };

			// A10 management port seems to implement a fairly broken HTTPS
			// stack which
			// does not support re-negotiation
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

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
	List<okhttp3.ConnectionSpec> getA10CompatibleConnectionSpecs() {
		List<okhttp3.ConnectionSpec> list = Lists.newArrayList();

		list.add(new okhttp3.ConnectionSpec.Builder(okhttp3.ConnectionSpec.MODERN_TLS)
				.tlsVersions(okhttp3.TlsVersion.TLS_1_0).build()); // This
		// is
		// essential
		list.add(okhttp3.ConnectionSpec.MODERN_TLS);
		list.add(okhttp3.ConnectionSpec.CLEARTEXT);
		return ImmutableList.copyOf(list);
	}

	@Override
	public boolean isActive() {
		try {
			Element e = invokeXml("ha.group.fetchStatistics");

			Element statusListElement = e.getChild("ha_group_status_list");
			if (statusListElement == null || statusListElement.getChildren().isEmpty()) {

				return true;
			}
			String x = statusListElement.getChildren().get(0).getChild("local_status").getText();
			if (Strings.nullToEmpty(x).trim().equals("1")) {
				return true;
			}
			return false;

		} catch (ElbException e) {
			throw e;
		} catch (RuntimeException e) {
			throw new ElbException(e);
		}
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("url", url).toString();
	}

	protected String formatUrl(Map<String, String> x, String format) {
		try {
			StringBuffer sb = new StringBuffer();
			sb.append(String.format("%s?session_id=%s&format=%s", getUrl(), URLEncoder.encode(getAuthToken(), UTF8),
					URLEncoder.encode(format, UTF8)));

			for (String key : x.keySet()) {
				if (!key.equals("format")) {
					String val = x.get(key);
					sb.append(String.format("&%s=%s", URLEncoder.encode(key, UTF8), URLEncoder.encode(val, UTF8)));

				}
			}

			return sb.toString();
		} catch (UnsupportedEncodingException e) {
			throw new ElbException(e);
		}
	}

	public RequestBuilder newRequest(String method) {
		RequestBuilder b = new RequestBuilder(this, method);

		return b;
	}

	protected String normalizeUrl(String url) {

		Preconditions.checkNotNull(url);
		Preconditions.checkArgument(url.startsWith("http://") || url.startsWith("https://"), "url must be http(s)");
		try {

			URL u = new URL(url);

			String normalized = u.getProtocol() + "://" + u.getHost() + ((u.getPort() > 0) ? ":" + u.getPort() : "")
					+ "/services/rest/v2/";

			return normalized;
		} catch (IOException e) {
			throw new IllegalArgumentException("invalid url: " + url);
		}
	}

	public ObjectNode executeJson(RequestBuilder b) {
		ObjectNode n = parseJsonResponse(execute(b), b.getMethod());
		throwExceptionIfNecessary(n);
		return n;

	}

	public Element executeXml(RequestBuilder b) {
		Element element = parseXmlResponse(execute(b), b.getMethod());
		throwExceptionIfNecessary(element);
		return element;

	}

	public Response execute(RequestBuilder b) {
		try {

			String method = b.getMethod();
			Preconditions.checkArgument(!Strings.isNullOrEmpty(method), "method argument must be passed");

			String url = null;

			okhttp3.Response resp;
			String format = b.getParams().getOrDefault("format", "xml");
			b = b.param("format", format);
			if (!b.hasBody()) {

				url = formatUrl(b.getParams(), format);
				FormBody.Builder fb = new FormBody.Builder().add("session_id", getAuthToken());
				for (Map.Entry<String, String> entry : b.getParams().entrySet()) {
					if (!entry.getValue().equals("format")) {
						fb = fb.add(entry.getKey(), entry.getValue());
					}
				}
				resp = getClient().newCall(new okhttp3.Request.Builder().url(getUrl()).post(fb.build()).build())
						.execute();
			} else if (b.getXmlBody().isPresent()) {
				b = b.param("format", "xml");
				url = formatUrl(b.getParams(), "xml");
				String bodyAsString = new XMLOutputter(Format.getRawFormat()).outputString(b.getXmlBody().get());
				final MediaType XML = MediaType.parse("text/xml");

				resp = getClient().newCall(new Request.Builder().url(url).post(RequestBody.create(XML, bodyAsString))
						.header("Content-Type", "text/xml").build()).execute();
			} else if (b.getJsonBody().isPresent()) {
				b = b.param("format", "json");
				url = formatUrl(b.getParams(), "json");
				String bodyAsString = mapper.writeValueAsString(b.getJsonBody().get());

				final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
				resp = getClient().newCall(new Request.Builder().url(url).post(

						RequestBody.create(JSON, bodyAsString)).header("Content-Type", "application/json").build())
						.execute();
			} else {
				throw new UnsupportedOperationException("body type not supported");
			}
			// the A10 API rather stupidly uses 200 responses even when there is
			// an error
			if (!resp.isSuccessful()) {
				logger.warn("response code={}", resp.code());
			}

			return resp;

		} catch (IOException e) {
			throw new ElbException(e);
		}
	}

}
