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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseDefinitionNotFoundException;
import com.wks.caseengine.cases.definition.service.CaseDefinitionService;
import com.wks.caseengine.rest.exception.RestResourceNotFoundException;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("case-definition")
@Tag(name = "Case Definition", description = "A Case Definition is the 'template' for the creation of new Cases Instances. If defines which attributes, stages and processes definitions will be used by Cases Instances created from it")
public class CaseDefinitionController {

	@Autowired
	private CaseDefinitionService caseDefinitionService;

	@GetMapping
	public ResponseEntity<List<CaseDefinition>> find(@RequestParam(required = false) Boolean deployed) {
		return ResponseEntity.ok(caseDefinitionService.find(Optional.ofNullable(deployed)));
	}

	@GetMapping(value = "/{caseDefId}")
	public ResponseEntity<CaseDefinition> get(@PathVariable final String caseDefId) {
		try {
			return ResponseEntity.ok(caseDefinitionService.get(caseDefId));
		} catch (CaseDefinitionNotFoundException e) {
			throw new RestResourceNotFoundException(e.getMessage());
		}
	}

	@PostMapping
	public ResponseEntity<CaseDefinition> save(@RequestBody final CaseDefinition caseDefinition) {
		return ResponseEntity.ok(caseDefinitionService.create(caseDefinition));
	}

	@PutMapping(value = "/{caseDefId}")
	public ResponseEntity<CaseDefinition> update(@PathVariable final String caseDefId,
			@RequestBody final CaseDefinition caseDefinition) {
		try {
			return ResponseEntity.ok(caseDefinitionService.update(caseDefId, caseDefinition));
		} catch (CaseDefinitionNotFoundException e) {
			throw new RestResourceNotFoundException(e.getMessage());
		}
	}

	@DeleteMapping(value = "/{caseDefId}")
	public ResponseEntity<Void> delete(@PathVariable final String caseDefId) {
		try {
			caseDefinitionService.delete(caseDefId);
		} catch (CaseDefinitionNotFoundException e) {
			throw new RestResourceNotFoundException(e.getMessage());
		}
		return ResponseEntity.noContent().build();
	}
}
