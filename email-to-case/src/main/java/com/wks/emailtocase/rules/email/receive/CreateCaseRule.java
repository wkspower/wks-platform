package com.wks.emailtocase.rules.email.receive;

import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Message.RecipientType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.caseengine.cases.businesskey.BusinessKeyGenerator;
import com.wks.caseengine.cases.instance.CaseAttribute;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.CaseInstanceService;

@Component
public class CreateCaseRule {

	@Autowired
	private CaseInstanceService caseInstanceService;

	@Autowired
	private BusinessKeyGenerator businessKeyGenerator;

	public void execute(final Message mailMessage, final String caseDefinitionId) throws Exception {

		Address[] from = mailMessage.getFrom();
		Address[] to = mailMessage.getRecipients(RecipientType.TO);
		String subject = mailMessage.getSubject();
		Object content = mailMessage.getContent();
		Date receivedDate = mailMessage.getReceivedDate();

		CaseInstance caseInstance = CaseInstance
				.builder().businessKey(businessKeyGenerator.generate()).caseDefinitionId(caseDefinitionId).attributes(
						Arrays.asList(

								CaseAttribute.builder().name("from")
										.value(Arrays.stream(from).map(o -> String.valueOf(o))
												.collect(Collectors.joining()))
										.build(),
								CaseAttribute.builder().name("to")
										.value(Arrays.stream(to).map(o -> String.valueOf(o))
												.collect(Collectors.joining()))
										.build(),
								CaseAttribute.builder().name("subject").value(subject).build(),
								CaseAttribute.builder().name("content").value(String.valueOf(content)).build(),
								CaseAttribute.builder().name("receivedDate").value(String.valueOf(receivedDate)).build()

						)).build();

		caseInstanceService.create(caseInstance);
	}

}
