package com.wks.mailbridge.client;

import java.util.Arrays;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.mailbridge.rules.email.receive.CreateCaseRule;

@Component
public class ReceiveMailService {

	@Autowired
	private CreateCaseRule createCaseRule;

	public void receive() throws Exception {
		Message[] messages = checkMailInbox();

		Arrays.stream(messages).forEach(o -> {
			try {
				createCaseRule.execute(o, "7");
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

	}

	public Message[] checkMailInbox() throws Exception {
		// create session object
		Session session = this.getImapSession();
		try {
			// connect to message store
			Store store = session.getStore("imap");
			store.connect("localhost", "", "");
			// open the inbox folder
			Folder inbox = (Folder) store.getFolder("INBOX");
			inbox.open(Folder.READ_WRITE);

			// fetch messages
			return inbox.getMessages();
		} catch (AuthenticationFailedException e) {
			throw new Exception(e);
		} catch (MessagingException e) {
			throw new Exception(e);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	private Session getImapSession() {
		Properties props = new Properties();
		props.setProperty("mail.store.protocol", "imap");
		props.setProperty("mail.debug", "true");
		props.setProperty("mail.imap.host", "localhost");
		props.setProperty("mail.imap.port", "143");
		props.setProperty("mail.imap.ssl.enable", "false");
		Session session = Session.getDefaultInstance(props, null);
		session.setDebug(true);
		return session;
	}

}
