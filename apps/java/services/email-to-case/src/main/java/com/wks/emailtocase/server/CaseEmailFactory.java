/*
 * WKS Platform - Open-Source Project
 * 
 * This file is part of the WKS Platform, an open-source project developed by WKS Power.
 * 
 * WKS Platform is licensed under the MIT License.
 * 
 * © 2021 WKS Power. All rights reserved.
 * 
 * For licensing information, see the LICENSE file in the root directory of the project.
 */
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

	public CaseEmail create(final String to, final String from, final String subject, final String body) {

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

		return caseEmailBuilder.to(to).from(from).subject(subject).body(body).build();

	}

}
