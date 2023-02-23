package com.wks.emailtocase.client;

import java.util.Arrays;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.emailtocase.rules.email.receive.CreateCaseRule;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
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

			// TODO: should be replaced by 'search' method as follows when working with
			// production mail server:
			// Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN),
			// false));
			Message[] messages = Arrays.stream(inbox.getMessages()).filter(o -> {
				try {
					return !(o.getFlags().contains(Flags.Flag.SEEN));
				} catch (MessagingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				return false;
			}).toArray(Message[]::new);

			log.info("New messages received: " + messages.length);

			Arrays.sort(messages, (m1, m2) -> {
				try {
					return m2.getSentDate().compareTo(m1.getSentDate());
				} catch (MessagingException e) {
					throw new RuntimeException(e);
				}
			});

			for (Message message : messages) {
				message.setFlag(Flags.Flag.SEEN, true);
			}

			return messages;

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
