package com.wks.emailtocase.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.wks.emailtocase.caseemail.CaseEmail;
import com.wks.emailtocase.caseemail.CaseEmailType;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CaseEmailFactory {

	@Value("${email-to-case.routing.new-case.pattern}")
	private String emailToCaseRoutingNewCasePattern;

	@Value("${email-to-case.routing.update-case.pattern}")
	private String emailToCaseRoutingUpdateCasePattern;

	public CaseEmail create(final String to, final String from, final String subject, final String text,
			final String html) throws CaseEmailBuilderException {

		CaseEmail.CaseEmailBuilder caseEmailBuilder = CaseEmail.builder();

		if (to.contains(emailToCaseRoutingNewCasePattern)) {

			log.debug("new case pattern identified");

			String caseDefinitionId = to.split("@")[0];
			
			log.debug("Case definition: " + caseDefinitionId);

			caseEmailBuilder

					.caseEmailType(CaseEmailType.NEW_CASE_EMAIL)

					.caseDefinitionId(caseDefinitionId);

		} else if (to.contains(emailToCaseRoutingUpdateCasePattern)) {
			log.debug("update case pattern identified");

			String caseInstanceBusinessKey = to.split("@")[0];
			
			log.debug("Case Business Key: " + caseInstanceBusinessKey);

			caseEmailBuilder

					.caseEmailType(CaseEmailType.UPDATE_CASE_EMAIL)

					.caseInstanceBusinessKey(caseInstanceBusinessKey);

		} else {
			throw new CaseEmailBuilderException("Could not retrieve Case Definition or Case Instance from Email");
		}

		return caseEmailBuilder.to(to).from(from).subject(subject).text(text).html(html).build();

	}

}
