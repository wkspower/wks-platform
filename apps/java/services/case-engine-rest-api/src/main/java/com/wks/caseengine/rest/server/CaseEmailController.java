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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.cases.instance.CaseInstanceNotFoundException;
import com.wks.caseengine.cases.instance.email.CaseEmail;
import com.wks.caseengine.cases.instance.email.CaseEmailService;
import com.wks.caseengine.exception.RestResourceNotFoundException;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("case-email")
@Tag(name = "Case Email", description = "Email to case API")
@Slf4j
public class CaseEmailController {

	@Autowired
	private CaseEmailService caseEmailService;
	
	@GetMapping
	public ResponseEntity<List<CaseEmail>> find(@RequestParam(required = false) String caseInstanceBusinessKey,
			@RequestParam(required = false) String caseDefinitionId) {

		return ResponseEntity.ok(caseEmailService.find(Optional.ofNullable(caseInstanceBusinessKey)));
	}

	@PostMapping
	public ResponseEntity<Void> start(@RequestBody final CaseEmail caseEmail) {
        System.out.println("in case email start 123"+ caseEmail.toString());
		caseEmail.setFrom("rakeshittam27@gmail.com");
		//caseEmail.setCaseDefinitionId("send-email-outbound");
		log.debug("### Email processing started ###");
		log.debug("To: " + caseEmail.getTo());
		log.debug("From: " + caseEmail.getFrom());
		log.debug("Subject: " + caseEmail.getSubject());
		log.debug("Body: " + caseEmail.getBody());
		

		log.debug("Definition Id: " + caseEmail.getCaseDefinitionId());

		caseEmailService.start(caseEmail);
        System.out.println("in case email start 123 end");
		log.debug("### Email processing finished ###");
		return ResponseEntity.noContent().build();

	}

	@PostMapping(value = "/save")
	public ResponseEntity<CaseEmail> save(@RequestBody final CaseEmail caseEmail) {
		log.debug("### Email processing started ###");
		log.debug("To: " + caseEmail.getTo());
		log.debug("From: " + caseEmail.getFrom());
		log.debug("Subject: " + caseEmail.getSubject());
		log.debug("Body: " + caseEmail.getBody());
		log.debug("Definition Id: " + caseEmail.getCaseDefinitionId());

		log.debug("### Email processing finished ###");
		return ResponseEntity.ok(caseEmailService.save(caseEmail));

	}

	@PatchMapping(value = "/{id}/sent", consumes = "application/merge-patch+json")
	public ResponseEntity<CaseEmail> markAsSent(@PathVariable final String id,
			@RequestBody final CaseEmail mergePatch) {

		try {
			System.out.println("in case email markAsSent 123"+ mergePatch.toString());
			caseEmailService.markAsSent(id, mergePatch.getReceivedDateTime());
		} catch (CaseInstanceNotFoundException e) {
			throw new RestResourceNotFoundException(e.getMessage());
		}

		return ResponseEntity.noContent().build();
	}

	@PatchMapping(value = "/{id}", consumes = "application/merge-patch+json")
	public ResponseEntity<CaseEmail> mergePatch(@PathVariable final String id, @RequestBody final CaseEmail mergePatch) {
		try {
			System.out.println("in case email mergePatch 123"+ mergePatch.toString());
			caseEmailService.patch(id, mergePatch);
		} catch (CaseInstanceNotFoundException e) {
			throw new RestResourceNotFoundException(e.getMessage());
		}

		return ResponseEntity.noContent().build();
	}

}
