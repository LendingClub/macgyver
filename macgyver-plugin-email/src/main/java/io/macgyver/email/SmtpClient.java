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
package io.macgyver.email;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.assertj.core.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.google.common.collect.Lists;

public class SmtpClient {

	Logger logger = LoggerFactory.getLogger(SmtpClient.class);
	Session session;

	public SmtpClient() {

	}

	public SmtpClient(Session session) {
		this.session = session;
	}



	public void sendMail(String from, String to, String subject, String body) {
		List<String> recipientList = Lists.newArrayList();
		recipientList.add(to);
		sendMail(from, recipientList, subject, body);
	}

	public void sendMail(String from, List<String> to, String subject,
			String body) {
		sendMail(from, to, subject, body, "", "");
	}
	
	public void sendMail(String from, List<String> to, String subject, String body, String attachmentFilename, String attachment) {
		File file = new File(attachmentFilename);

		if (!Strings.isNullOrEmpty(attachmentFilename) && !Strings.isNullOrEmpty(attachment)) {
			try { 
				file.createNewFile();
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(attachment);
				bw.close();
				
				sendMail(from, to, subject, body, attachmentFilename, file);
			} catch (IOException e) { 
				throw new MailException(e);
			}
		} else { 
			sendMail(from, to, subject, body, "", file); 
		}
	}
	
	public void sendMail(String from, List<String> to, String subject, String body, String attachmentFilename, File file) {
		try {
			 
			logger.debug("sending email from:{} to:{}",from,to);
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			List<InternetAddress> addressList = Lists.newArrayList();
			for (String addr: to) {
				InternetAddress[] address = InternetAddress.parse(addr);
				if (address!=null) {
					for (InternetAddress x: address) {
						addressList.add(x);
					
					}
				}
			}
			message.setRecipients(Message.RecipientType.TO,addressList.toArray(new InternetAddress[0]));
			message.setSubject(subject);
			message.setText(body);
			
			if (!Strings.isNullOrEmpty(attachmentFilename)) {
				MimeMessageHelper helper = new MimeMessageHelper((MimeMessage)message, true);
				helper.addAttachment(attachmentFilename, file);
			}
 
			Transport.send(message);
 
		} catch (MessagingException e) {
			throw new MailException(e);
		}
	}
}
