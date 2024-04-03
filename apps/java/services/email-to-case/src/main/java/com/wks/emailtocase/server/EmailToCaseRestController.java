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

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.emailtocase.caseemail.CaseEmail;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("case-email")
@Tag(name = "Email to case", description = "Email to case API")
@Slf4j
public class EmailToCaseRestController {

//	@Autowired
//	private CaseEmailService caseEmailService;

//	@Autowired
//	private CaseInstanceService caseInstanceService;
//
//	@Autowired
//	private CaseEmailFactory caseEmailFactory;

	@PostMapping
	public ResponseEntity<Void> save(@RequestBody final CaseEmail caseEmail) {

		log.debug("### Email received - Processing started ###");
		log.debug("To: " + caseEmail.getTo());
		log.debug("From: " + caseEmail.getFrom());
		log.debug("Subject: " + caseEmail.getSubject());
		log.debug("Body: " + caseEmail.getBody());
		log.debug("Definition Id: " + caseEmail.getCaseDefinitionId());

//		CaseEmail caseEmail = caseEmailFactory.create(to, from, subject, body);
//
//		if (caseEmail.getCaseEmailType().equals(CaseEmailType.NEW_CASE_EMAIL)) {
//			CaseInstance caseInstance = caseInstanceService.startEmpty(caseEmail.getCaseDefinitionId());
//			caseEmail.setCaseInstanceBusinessKey(caseInstance.getBusinessKey());
//
//			caseEmailService.save(caseEmail);
//
//		} else if (caseEmail.getCaseEmailType().equals(CaseEmailType.UPDATE_CASE_EMAIL)) {
//			caseEmailService.save(caseEmail);
//		}
//
//		log.debug("### Email received - Processing finished ###");

		return ResponseEntity.noContent().build();

	}

	@GetMapping
	public ResponseEntity<List<CaseEmail>> find(@RequestParam(required = false) String caseInstanceBusinessKey,
			@RequestParam(required = false) String caseDefinitionId) {

//		return ResponseEntity.ok(caseEmailService.find(Optional.ofNullable(caseInstanceBusinessKey),
//				Optional.ofNullable(caseDefinitionId)));
		return ResponseEntity.ok(null);
	}

}
