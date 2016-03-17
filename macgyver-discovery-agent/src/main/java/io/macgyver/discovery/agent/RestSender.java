package io.macgyver.discovery.agent;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

public class RestSender extends Sender {

	

	@Override
	public void send(Properties p) throws IOException {
		
		String url = props.getProperty("url");
		
		
		URL u = new URL(url);
		
		HttpURLConnection c = (HttpURLConnection) u.openConnection();
		
		
		c.setDoOutput(true);
		c.setRequestMethod("POST");
		
		OutputStream out = c.getOutputStream();
		
		OutputStreamWriter w = new OutputStreamWriter(out);
		
		log.finer("sending to "+url+" "+toFormEncodedString(p));
		
		w.write(toFormEncodedString(p));
		
		w.close();
		
		int rc = c.getResponseCode();
		log.finer("rc: "+rc);
		// macgyver wants to see a form-encoded POST
		
		
	}

}
