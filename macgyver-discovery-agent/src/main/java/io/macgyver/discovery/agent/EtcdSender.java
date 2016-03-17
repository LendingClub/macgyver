package io.macgyver.discovery.agent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

public class EtcdSender extends Sender {

	java.util.logging.Logger log = java.util.logging.Logger
			.getLogger(EtcdSender.class.getName());

	String url = "http://localhost:4001/v2/keys";

	private void checkRequiredProperty(String property, String val) {
		if (val == null || val.length() < 1) {

			throw new IllegalArgumentException(property + " must be specified");
		}
	}


	protected String stem(String env, String app, String host) {

		checkRequiredProperty("env", env);
		checkRequiredProperty("app", app);
		checkRequiredProperty("host", host);

		return String.format("/macgyver/env/%s/%s/%s", env, app, host);
	}

	public void send(String url, String val) throws IOException {
		
		
		URL u = new URL(url);
		
		if (log.isLoggable(Level.FINER)) {
			log.finer(String.format("PUT: %s",url));
		}
	
		HttpURLConnection c = (HttpURLConnection) u.openConnection();
		c.setReadTimeout(5000);
		c.setConnectTimeout(5000);
		
		c.setRequestMethod("PUT");
		c.setDoOutput(true);
		c.setDoInput(true);
		c.addRequestProperty("Content-type", "x-www-form-urlencoded");

		OutputStream os = c.getOutputStream();

		PrintWriter pw = new PrintWriter(new OutputStreamWriter(os));
		
		Properties form = new Properties();
		form.put("value", val);
		pw.print(toFormEncodedString(form));
		
		pw.close();

		int responseCode = c.getResponseCode();
		
		if (log.isLoggable(Level.FINER)) {
			log.finer(String.format("response code: %d",responseCode));
		}
		
		
		// X-Raft-Index
		// X-Raft-Term
		// X-Etcd-Cluster-Id
		// X-Etcd-Index
		
		
		byte [] buffer = new byte[1000];
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream input = c.getInputStream();
		int count=0;
		while ((count=input.read(buffer))>0) {
			baos.write(buffer,0,count);
		}
		
		
		c.disconnect();
		

	}

	@Override
	public void send(Properties p) {

		String stem = stem(p.getProperty("env"), p.getProperty("app"),
				p.getProperty("host"));

		for (Map.Entry entry : p.entrySet()) {

			String fullUrl = null;
			try {
				String key = entry.getKey().toString();
				String val = entry.getKey().toString();

				String fullKey = stem + "/" + key;

				fullUrl = url + fullKey;

				send(fullUrl, val);
			} catch (IOException e) {
				log.log(java.util.logging.Level.WARNING, "problem sending to "+fullUrl+": "+e.toString());
			}

		}
	}

}
