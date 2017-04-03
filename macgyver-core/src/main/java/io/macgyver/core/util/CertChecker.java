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
package io.macgyver.core.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.ISODateTimeFormat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class CertChecker {

	public static final String DESCRIPTION = "description";
	int certExpirationWarningDays = 30;

	public int getCertExpirationWarningDays() {
		return this.certExpirationWarningDays;
	}

	public void setCertExpirationWarningDays(int d) {
		this.certExpirationWarningDays = d;
	}

	public java.security.cert.X509Certificate convert(
			javax.security.cert.X509Certificate cert) {
		try {
			byte[] encoded = cert.getEncoded();
			ByteArrayInputStream bis = new ByteArrayInputStream(encoded);
			java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory
					.getInstance("X.509");
			return (java.security.cert.X509Certificate) cf
					.generateCertificate(bis);
		} catch (java.security.cert.CertificateEncodingException e) {
		} catch (javax.security.cert.CertificateEncodingException e) {
		} catch (java.security.cert.CertificateException e) {
		}
		return null;
	}

	public class CertExtractor implements HostnameVerifier {
		List<X509Certificate> certList = new ArrayList<X509Certificate>();
		String requestedHost = null;

		@Override
		public boolean verify(String hostname, SSLSession session) {
			requestedHost = hostname;

			try {
				javax.security.cert.X509Certificate[] certs = session
						.getPeerCertificateChain();
				for (javax.security.cert.X509Certificate cert : certs) {

					X509Certificate xx = convert(cert);
					certList.add(xx);

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}

	}

	public boolean doesCertificateHostnameMatch(List<X509Certificate> certList,
			String hostname) {
		try {
			X509Certificate cert = certList.get(0);
			String cn = extractCN(cert);

			if (Strings.isNullOrEmpty(cn)) {
				// fall through
			} else {
				if (hostname != null) {
					if (hostname.equalsIgnoreCase(cn)) {
						return true;
					}
					if (cn.startsWith("*")) {
						return hostname.toLowerCase().endsWith(
								cn.substring(1).toLowerCase());
					}
				}

			}
			for (List alternativeNames : cert.getSubjectAlternativeNames()) {
				for (Object val : alternativeNames) {
					if (hostname.equalsIgnoreCase(Objects.toString(val, ""))) {
						return true;
					}

				}
			}

			return false;
		} catch (Exception e) {
			// ignore
		}
		return false;
	}

	public String extractCN(X509Certificate cert)
			throws GeneralSecurityException {
		X500Name x500name = new JcaX509CertificateHolder(cert).getSubject();
		RDN cn = x500name.getRDNs(BCStyle.CN)[0];

		String valx = IETFUtils.valueToString(cn.getFirst().getValue());
		return valx;
	}

	private List<ObjectNode> checkCertChain(String url) throws IOException {
		List<ObjectNode> problems = new ArrayList<ObjectNode>();
		try {
			OkHttpClient c = new OkHttpClient();
			Request.Builder b = new Request.Builder().url(url);
			c.setHostnameVerifier(io.macgyver.core.jaxrs.SslTrust.withoutHostnameVerification());
			Response response = c.newCall(b.build()).execute();
			response.body().close();
		} catch (Exception e) {
			if (e.toString().contains("PKIX path building failed")) {
				ObjectNode n = new ObjectMapper().createObjectNode();
				n.put(DESCRIPTION, "invalid certficate chain");
				problems.add(n);
			}

		}
		return problems;

	}

	public ObjectNode checkCertificates(String url)
			throws GeneralSecurityException, IOException {
		return checkCertificates(url, null);
	}

	public ObjectNode checkCertificates(String url, String alternateHost)
			throws IOException, GeneralSecurityException {
		ObjectNode n = new ObjectMapper().createObjectNode();
		return checkCertificates(n, url, alternateHost);
	}

	protected ObjectNode checkCertificates(ObjectNode n, String url,
			String alternateHost) throws IOException, GeneralSecurityException {
		ObjectMapper mapper = new ObjectMapper();

		ArrayNode problems = mapper.createArrayNode();
		n.set("problems", problems);
		n.set("certs", mapper.createArrayNode());
		try {
			URL xurl = new URL(url);
			String host = xurl.getHost();
			n.put("host", alternateHost != null ? alternateHost : host);
			n.put("url", url);
			List<X509Certificate> certs = fetchCertificates(url);

			n = checkCertificates(n, certs,
					alternateHost != null ? alternateHost : host);

			try {

				List<ObjectNode> chainErrors = checkCertChain(url);
				ArrayNode errors = (ArrayNode) n.path("problems");
				for (ObjectNode x : chainErrors) {
					errors.add(x);
				}
			} catch (Exception e) {
				((ArrayNode) n.path("problems")).add(new ObjectMapper()
						.createObjectNode().put("type", "error")
						.put(DESCRIPTION, e.toString()));
			}
		} catch (Exception e) {
			problems.add(mapper.createObjectNode().put("type", "error")
					.put("description", e.toString()));
		}
		return n;

	}

	public ObjectNode checkCertificates(List<X509Certificate> certs) {
		return checkCertificates(certs, null);
	}

	public ObjectNode checkCertificates(List<X509Certificate> certs,
			String alternateHost) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode results = mapper.createObjectNode();
		ArrayNode certArray = mapper.createArrayNode();
		ArrayNode problems = mapper.createArrayNode();
		results.set("problems", problems);
		results.set("certs", certArray);
		return checkCertificates(results, certs, alternateHost);
	}

	protected ObjectNode checkCertificates(ObjectNode nx,
			List<X509Certificate> certs, String alternateHost) {
		int fewestDaysToExpiration = Integer.MAX_VALUE;
		ObjectMapper mapper = new ObjectMapper();
		ArrayNode problems = (ArrayNode) nx.get("problems");
		ArrayNode certArray = (ArrayNode) nx.get("certs");

		try {

			for (X509Certificate cert : certs) {
				ObjectNode n = mapper.createObjectNode();
				String subjectDN = cert.getSubjectDN().getName();
				n.put("subjectDN", subjectDN);

				n.put("subjectCN", extractCN(cert));
				n.put("version", cert.getVersion());
				n.put("issuerDN", cert.getIssuerDN().getName());
				n.put("notValidAfter",
						ISODateTimeFormat.dateTime().print(
								cert.getNotAfter().getTime()));
				n.put("notValidAfterTimestamp", cert.getNotAfter().getTime());
				n.put("notValidBefore",
						ISODateTimeFormat.dateTime().print(
								cert.getNotBefore().getTime()));
				n.put("notValidBeforeTimestamp", cert.getNotBefore().getTime());
				n.put("serial", cert.getSerialNumber().toString());
				n.put("isDateValid", true);
				n.put("version", cert.getVersion());
				n.put("type", cert.getType());

				if (System.currentTimeMillis() < cert.getNotBefore().getTime()) {
					problems.add(mapper.createObjectNode()
							.put(DESCRIPTION, "certificate not yet valid")
							.put("type", "error"));

					n.put("isDateValid", false);
				}
				if (System.currentTimeMillis() > cert.getNotAfter().getTime()) {

					problems.add(mapper.createObjectNode()
							.put(DESCRIPTION, "certificate expired")
							.put("type", "error"));

					n.put("isDateValid", false);
				}
				int daysToExpiration = Math.max(
						0,
						Days.daysBetween(
								new DateTime(System.currentTimeMillis()),
								new DateTime(cert.getNotAfter())).getDays());
				n.put("daysToExpiration", daysToExpiration);
				fewestDaysToExpiration = Math.min(fewestDaysToExpiration,
						daysToExpiration);

				certArray.add(n);

				Collection<List<?>> altNames = cert
						.getSubjectAlternativeNames();
				ArrayNode altNameArray = mapper.createArrayNode();
				n.set("subjectAlternativeNames", altNameArray);
				// http://stackoverflow.com/questions/18775206/extracting-subjectalternativename-from-x509-in-java
				if (altNames != null) {
					for (List altList : altNames) {
						if (altList.size() == 2) {
							ObjectNode altData = mapper.createObjectNode();
							altData.put("type",
									Integer.parseInt(altList.get(0).toString()));
							altData.put("value", altList.get(1).toString());

							altNameArray.add(altData);
						}
					}
				}

			}

		} catch (Exception e) {
			problems.add(mapper.createObjectNode().put("type", "error")
					.put(DESCRIPTION, e.toString()));
		}

		if (!doesCertificateHostnameMatch(certs, alternateHost)) {
			problems.add(mapper.createObjectNode()
					.put(DESCRIPTION, "host name does not match certificate")
					.put("type", "error"));

		}
		if (fewestDaysToExpiration <= getCertExpirationWarningDays()) {
			problems.add(mapper
					.createObjectNode()
					.put(DESCRIPTION,
							"certificate will expire in "
									+ fewestDaysToExpiration + " days")
					.put("type", "warning"));
		}

		nx.put("daysToExpiration", fewestDaysToExpiration);
		return nx;
	}

	public List<X509Certificate> fetchCertificates(String httpUrl)
			throws IOException {
		OkHttpClient c = new OkHttpClient();

		CertExtractor extractor = new CertExtractor();
		c.setHostnameVerifier(extractor);
		c.setSslSocketFactory(io.macgyver.core.jaxrs.SslTrust.withoutCertificateValidation()
				.getSocketFactory());

		Request r = new Request.Builder().url(httpUrl).build();

		Response response = c.newCall(r).execute();
		response.body().close();

		return extractor.certList;

	}

}
