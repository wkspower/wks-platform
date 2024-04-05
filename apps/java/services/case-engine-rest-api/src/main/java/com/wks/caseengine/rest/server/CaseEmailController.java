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
package com.wks.caseengine.rest.server;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.cases.instance.email.CaseEmail;
import com.wks.caseengine.cases.instance.email.CaseEmailService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("case-email")
@Tag(name = "Case Email", description = "Email to case API")
@Slf4j
public class CaseEmailController {

	@Autowired
	private CaseEmailService caseEmailService;

	@PostMapping
	public ResponseEntity<Void> start(@RequestBody final CaseEmail caseEmail) {
		log.debug("### Email processing started ###");
		log.debug("To: " + caseEmail.getTo());
		log.debug("From: " + caseEmail.getFrom());
		log.debug("Subject: " + caseEmail.getSubject());
		log.debug("Body: " + caseEmail.getBody());
		log.debug("Definition Id: " + caseEmail.getCaseDefinitionId());

		caseEmailService.start(caseEmail);

		log.debug("### Email processing finished ###");
		return ResponseEntity.noContent().build();

	}

	@PostMapping(value = "/save")
	public ResponseEntity<Void> save(@RequestBody final CaseEmail caseEmail) {
		log.debug("### Email processing started ###");
		log.debug("To: " + caseEmail.getTo());
		log.debug("From: " + caseEmail.getFrom());
		log.debug("Subject: " + caseEmail.getSubject());
		log.debug("Body: " + caseEmail.getBody());
		log.debug("Definition Id: " + caseEmail.getCaseDefinitionId());

		caseEmailService.save(caseEmail);

		log.debug("### Email processing finished ###");
		return ResponseEntity.noContent().build();

	}

	@GetMapping
	public ResponseEntity<List<CaseEmail>> find(@RequestParam(required = false) String caseInstanceBusinessKey,
			@RequestParam(required = false) String caseDefinitionId) {

		return ResponseEntity.ok(caseEmailService.find(Optional.ofNullable(caseInstanceBusinessKey)));
	}

}
