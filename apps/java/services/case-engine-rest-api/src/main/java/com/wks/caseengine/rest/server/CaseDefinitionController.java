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
import com.wks.caseengine.cmmn.CmmnImportResult;
import com.wks.caseengine.cmmn.CmmnImportService;
import com.wks.caseengine.cmmn.parse.CmmnParseException;
import com.wks.caseengine.rest.exception.RestInvalidArgumentException;
import com.wks.caseengine.rest.exception.RestResourceNotFoundException;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("case-definition")
@Tag(name = "Case Definition", description = "A Case Definition is the 'template' for the creation of new Cases Instances. If defines which attributes, stages and processes definitions will be used by Cases Instances created from it")
public class CaseDefinitionController {

	@Autowired
	private CaseDefinitionService caseDefinitionService;

	@Autowired
	private CmmnImportService cmmnImportService;

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
		try {
			return ResponseEntity.ok(caseDefinitionService.create(caseDefinition));
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("caseDefinitionId", e);
		}
	}

	/**
	 * Imports a CMMN 1.1 model as a case definition, generating the forms and the
	 * BPMN processes that make it runnable.
	 *
	 * <p>Deliberately mounted under {@code case-definition} rather than at a path of
	 * its own: authorization is evaluated on the first URI segment, so this inherits
	 * the case-definition manager rule and needs no new policy. A new top-level path
	 * would be denied by default until the policy bundle was updated.
	 *
	 * <p>With {@code dryRun=true} nothing is written — the same result is returned so
	 * the caller can review the warnings, which is how the mapping's assumptions get
	 * checked before anything is created.
	 *
	 * @param cmmnXml the model, as the raw request body
	 */
	@PostMapping(value = "/import/cmmn", consumes = { "application/xml", "text/xml", "text/plain",
			"application/octet-stream" })
	public ResponseEntity<CmmnImportResult> importCmmn(@RequestBody final String cmmnXml,
			@RequestParam(required = false, defaultValue = "false") final boolean dryRun) {
		try {
			return ResponseEntity.ok(cmmnImportService.importModel(cmmnXml, dryRun));
		} catch (CmmnParseException e) {
			// Single-arg on purpose: the parser's message already names what to do
			// about it (e.g. "that is a rendered diagram, export CMMN 1.1 XML"), and
			// the generic wording would throw that explanation away.
			throw new RestInvalidArgumentException(e.getMessage());
		}
	}

	/**
	 * Attaches the diagram a case type was modelled as, so cases can show it.
	 *
	 * <p>Separate from the import because it is a separate artifact: the importer
	 * reads a CMMN <em>model</em>, while this is the <em>picture</em> of it, which
	 * cannot be derived from the model without a rendering library. Keeping it on its
	 * own endpoint also means a case type modelled elsewhere can be given a diagram
	 * without being re-imported.
	 *
	 * <p>Optional throughout — a case type with no diagram simply does not show one.
	 *
	 * @param svg the rendered diagram as SVG markup
	 */
	@PutMapping(value = "/{caseDefId}/source-diagram", consumes = { "image/svg+xml", "application/xml", "text/xml",
			"text/plain" })
	public ResponseEntity<Void> attachSourceDiagram(@PathVariable final String caseDefId,
			@RequestBody final String svg) {
		try {
			caseDefinitionService.attachSourceDiagram(caseDefId, svg);
			return ResponseEntity.noContent().build();
		} catch (CaseDefinitionNotFoundException e) {
			throw new RestResourceNotFoundException(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException(e.getMessage());
		}
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
