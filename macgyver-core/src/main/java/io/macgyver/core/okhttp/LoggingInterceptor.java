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
package io.macgyver.core.okhttp;

import com.squareup.okhttp.Connection;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import com.squareup.okhttp.internal.Platform;
import com.squareup.okhttp.internal.http.HttpEngine;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okio.Buffer;
import okio.BufferedSource;

/**
 * This is a modified version of Square's HttpLoggingInterceptor.
 * 
 * It is bound to SLF4J. All logging will performed at debug level. If the
 * logger is not set to debug level, the interceptor will be a no-op. If there
 * is an exception thrown during the logging operation, the exception is caught
 * and the interceptor chain continues. Some additional care is taken not to
 * print binary payloads.
 * 
 */
public class LoggingInterceptor implements Interceptor {
	private static final Charset UTF8 = Charset.forName("UTF-8");

	int maxPrintableBodySize = 1024 * 512;
	org.slf4j.Logger slf4j;

	public enum Level {
		/** No logs. */
		NONE,
		/**
		 * Logs request and response lines.
		 * <p>
		 * Example:
		 * 
		 * <pre>
		 * {@code
		 * --> POST /greeting HTTP/1.1 (3-byte body)
		 *
		 * <-- HTTP/1.1 200 OK (22ms, 6-byte body)
		 * }
		 * </pre>
		 */
		BASIC,
		/**
		 * Logs request and response lines and their respective headers.
		 * <p>
		 * Example:
		 * 
		 * <pre>
		 * {@code
		 * --> POST /greeting HTTP/1.1
		 * Host: example.com
		 * Content-Type: plain/text
		 * Content-Length: 3
		 * --> END POST
		 *
		 * <-- HTTP/1.1 200 OK (22ms)
		 * Content-Type: plain/text
		 * Content-Length: 6
		 * <-- END HTTP
		 * }
		 * </pre>
		 */
		HEADERS,
		/**
		 * Logs request and response lines and their respective headers and
		 * bodies (if present).
		 * <p>
		 * Example:
		 * 
		 * <pre>
		 * {@code
		 * --> POST /greeting HTTP/1.1
		 * Host: example.com
		 * Content-Type: plain/text
		 * Content-Length: 3
		 *
		 * Hi?
		 * --> END GET
		 *
		 * <-- HTTP/1.1 200 OK (22ms)
		 * Content-Type: plain/text
		 * Content-Length: 6
		 *
		 * Hello!
		 * <-- END HTTP
		 * }
		 * </pre>
		 */
		BODY
	}

	protected LoggingInterceptor(org.slf4j.Logger logger, Level level) {
		this.slf4j = logger;
		setLevel(level);
	}

	public static LoggingInterceptor create(String name) {
		return create(LoggerFactory.getLogger(name));
	}

	public static LoggingInterceptor create(Class claz) {
		return create(LoggerFactory.getLogger(claz));
	}

	public static LoggingInterceptor create(Logger logger) {
		return create(logger, Level.BODY);
	}

	public static LoggingInterceptor create(Logger logger, Level debugLevel) {
		return new LoggingInterceptor(logger, debugLevel);

	}

	private volatile Level level = Level.NONE;

	/** Change the level at which this interceptor logs. */
	public LoggingInterceptor setLevel(Level level) {
		if (level == null)
			throw new NullPointerException("level == null. Use Level.NONE instead.");
		this.level = level;
		return this;
	}

	public Level getLevel() {
		return level;
	}

	protected void log(String x) {
		if (level == Level.NONE) {
			// do nothing
		} else {
			if (slf4j.isDebugEnabled()) {
				slf4j.debug(x);
			}
		}

	}

	@Override
	public Response intercept(Chain chain) throws IOException {

		Level level = this.level;

		Request request = chain.request();
		if (level == Level.NONE || (!slf4j.isDebugEnabled())) {
			return chain.proceed(request);
		}

		boolean logBody = level == Level.BODY;
		boolean logHeaders = logBody || level == Level.HEADERS;

		try {

			RequestBody requestBody = request.body();
			boolean hasRequestBody = requestBody != null;

			Connection connection = chain.connection();
			Protocol protocol = connection != null ? connection.getProtocol() : Protocol.HTTP_1_1;
			String requestStartMessage = "--> " + request.method() + ' ' + request.httpUrl() + ' ' + protocol(protocol);
			if (!logHeaders && hasRequestBody) {
				requestStartMessage += " (" + requestBody.contentLength() + "-byte body)";
			}
			log(requestStartMessage);

			if (logHeaders) {
				if (hasRequestBody) {
					// Request body headers are only present when installed as a
					// network interceptor. Force
					// them to be included (when available) so there values are
					// known.
					if (requestBody.contentType() != null) {
						log("Content-Type: " + requestBody.contentType());
					}
					if (requestBody.contentLength() != -1) {
						log("Content-Length: " + requestBody.contentLength());
					}
				}

				Headers headers = request.headers();
				for (int i = 0, count = headers.size(); i < count; i++) {
					String name = headers.name(i);

					if (name.equalsIgnoreCase("authorization")) {
						log(name + ": ************");
					} else {
						// Skip headers from the request body as they are
						// explicitly
						// logged above.
						if (!"Content-Type".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase(name)) {
							log(name + ": " + headers.value(i));
						}
					}
				}

				if (!logBody || !hasRequestBody) {
					slf4j.debug("--> END " + request.method());
				} else if (bodyEncoded(request.headers())) {
					log("--> END " + request.method() + " (encoded body omitted)");
				} else {
					Buffer buffer = new Buffer();
					requestBody.writeTo(buffer);

					Charset charset = UTF8;
					MediaType contentType = requestBody.contentType();
					if (contentType != null) {
						contentType.charset(UTF8);
					}

					log("");
					String body = redactRequestBody(buffer.readString(charset));
					
					log(body);

					log("--> END " + request.method() + " (" + requestBody.contentLength() + "-byte body)");
				}
			}

		} catch (Exception e) {
			LoggerFactory.getLogger(LoggingInterceptor.class).warn("problem logging request: " + e); // no stack trace
		}
		long startNs = System.nanoTime();
		Response response = chain.proceed(request);
		try {
			long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

			ResponseBody responseBody = response.body();
			log("<-- " + protocol(response.protocol()) + ' ' + response.code() + ' ' + response.message() + " ("
					+ tookMs + "ms" + (!logHeaders ? ", " + responseBody.contentLength() + "-byte body" : "") + ')');

			if (logHeaders) {
				Headers headers = response.headers();
				for (int i = 0, count = headers.size(); i < count; i++) {
					log(headers.name(i) + ": " + headers.value(i));
				}

				if (!logBody || !HttpEngine.hasBody(response)) {
					log("<-- END HTTP");
				} else if (!isResponseBodyPrintable(response)) {
					log("<-- END HTTP (body omitted)");
				} else {
					BufferedSource source = responseBody.source();
					source.request(maxPrintableBodySize); // Buffer the entire body.
					Buffer buffer = source.buffer();

					Charset charset = UTF8;
					MediaType contentType = responseBody.contentType();
					if (contentType != null) {
						charset = contentType.charset(UTF8);
					}

					if (responseBody.contentLength() != 0) {
						log("");
						log(redactResponseBody(buffer.clone().readString(charset)));
					}

					log("<-- END HTTP (" + buffer.size() + "-byte body)");
				}
			}
		} catch (Exception e) {
			LoggerFactory.getLogger(LoggingInterceptor.class).warn("problem logging: " + e.toString());
		}

		return response;
	}

	protected boolean isResponseBodySizeWithinLimit(Response response) {
		try {
			if (Long.parseLong(response.header("Content-length", "0")) > maxPrintableBodySize) {
				return false;
			}
			return true;
		} catch (RuntimeException e) {
			slf4j.warn("problem reading content-length: " + e);
		}
		return false;
	}

	private boolean isResponseBodyPrintable(Response response) {
		String contentType = response.headers().get("Content-type");
		if (contentType != null) {
			if (contentType.contains("image") || contentType.contains("octet-stream")) {
				return false;
			}
		}
		if (!isResponseBodySizeWithinLimit(response)) {
			return false;
		}

		return true;
	}

	private boolean bodyEncoded(Headers headers) {

		String contentEncoding = headers.get("Content-Encoding");
		return contentEncoding != null && !contentEncoding.equalsIgnoreCase("identity");
	}

	private static String protocol(Protocol protocol) {
		return protocol == Protocol.HTTP_1_0 ? "HTTP/1.0" : "HTTP/1.1";
	}
	
	protected String redactResponseBody(String s) {
		if (s==null) {
			return s;
		}
		
		return s;
	}
	protected String redactRequestBody(String s) {
		if (s==null) {
			return s;
		}
		
		return s;
		
	}
}
