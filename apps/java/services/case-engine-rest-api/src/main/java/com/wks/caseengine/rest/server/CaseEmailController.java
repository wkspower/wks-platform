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
import com.wks.caseengine.message.vm.AOPMessageVM;

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
	public ResponseEntity<AOPMessageVM> find(@RequestParam(required = false) String caseInstanceBusinessKey,
			@RequestParam(required = false) String caseDefinitionId) {

		AOPMessageVM response = caseEmailService.find(Optional.ofNullable(caseInstanceBusinessKey));

		return ResponseEntity.status(response.getCode()).body(response);
	}

	@PostMapping
	public ResponseEntity<AOPMessageVM> start(@RequestBody final CaseEmail caseEmail) {
		System.out.println("in case email start 123" + caseEmail.toString());
		caseEmail.setFrom("rakeshittam27@gmail.com");
		// caseEmail.setCaseDefinitionId("send-email-outbound");
		log.debug("### Email processing started ###");
		log.debug("To: " + caseEmail.getTo());
		log.debug("From: " + caseEmail.getFrom());
		log.debug("Subject: " + caseEmail.getSubject());
		log.debug("Body: " + caseEmail.getBody());
		log.debug("Definition Id: " + caseEmail.getCaseDefinitionId());

		AOPMessageVM response = new AOPMessageVM();
		try {
			AOPMessageVM result = caseEmailService.start(caseEmail);
			log.debug("### Email processing finished ###");
			return ResponseEntity.status(result.getCode()).body(result);

		} catch (Exception e) {
			log.error("Error during email processing", e);
			response.setCode(500);
			response.setMessage("Email processing failed: " + e.getMessage());
			response.setData(null);
			return ResponseEntity.status(response.getCode()).body(response);
		}

	}

	@PostMapping(value = "/save")
	public ResponseEntity<AOPMessageVM> save(@RequestBody final CaseEmail caseEmail) {

		log.debug("### Email processing started ###");
		log.debug("To: " + caseEmail.getTo());
		log.debug("From: " + caseEmail.getFrom());
		log.debug("Subject: " + caseEmail.getSubject());
		log.debug("Body: " + caseEmail.getBody());
		log.debug("Definition Id: " + caseEmail.getCaseDefinitionId());

		AOPMessageVM response = caseEmailService.save(caseEmail);

		log.debug("### Email processing finished ###");
		return ResponseEntity.status(response.getCode()).body(response);

	}

	@PatchMapping(value = "/{id}/sent", consumes = "application/merge-patch+json")
	public ResponseEntity<AOPMessageVM> markAsSent(@PathVariable final String id,
			@RequestBody final CaseEmail mergePatch) {
		log.debug("### Marking case email as sent ###");
		try {
			System.out.println("in case email markAsSent 123" + mergePatch.toString());
			AOPMessageVM response = caseEmailService.markAsSent(id, mergePatch.getReceivedDateTime());
			return ResponseEntity.status(response.getCode()).body(response);
		} catch (CaseInstanceNotFoundException e) {
			throw new RestResourceNotFoundException(e.getMessage());
		}
	}

	@PatchMapping(value = "/{id}", consumes = "application/merge-patch+json")
	public ResponseEntity<AOPMessageVM> mergePatch(@PathVariable final String id,
			@RequestBody final CaseEmail mergePatch) {
		try {
			System.out.println("in case email mergePatch 123" + mergePatch.toString());
			AOPMessageVM response = caseEmailService.patch(id, mergePatch);
			return ResponseEntity.status(response.getCode()).body(response);
		} catch (CaseInstanceNotFoundException e) {
			throw new RestResourceNotFoundException(e.getMessage());
		}
	}

}
