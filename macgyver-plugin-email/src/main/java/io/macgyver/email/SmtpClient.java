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
package io.macgyver.email;

import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
			
 
			Transport.send(message);
 
			
 
		} catch (MessagingException e) {
			throw new MailException(e);
		}
	}
}
