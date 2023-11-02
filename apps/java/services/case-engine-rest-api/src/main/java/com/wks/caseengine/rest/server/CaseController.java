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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.GsonBuilder;
import com.wks.caseengine.cases.instance.CaseComment;
import com.wks.caseengine.cases.instance.CaseDocument;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.CaseInstanceFilter;
import com.wks.caseengine.cases.instance.CaseInstanceNotFoundException;
import com.wks.caseengine.cases.instance.service.CaseInstanceService;
import com.wks.caseengine.pagination.Cursor;
import com.wks.caseengine.pagination.PageResult;
import com.wks.caseengine.rest.exception.ResourceNotFoundException;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("case")
@Tag(name = "Case Instance", description = "A Case Instance is created based in a Case Definition and is the 'Digital Folder' for related information, documents, communication and processes for case")
public class CaseController {

	@Autowired
	private CaseInstanceService caseInstanceService;

	@Autowired
	private GsonBuilder gsonBuilder;

	@GetMapping
	public ResponseEntity<Object> find(@RequestParam(required = false) String status,
			@RequestParam(required = false) String caseDefinitionId,
			@RequestParam(required = false, name = "before") String before,
			@RequestParam(required = false, name = "after") String after,
			@RequestParam(required = false, name = "sort") String sort,
			@RequestParam(required = false, name = "limit") String limit) {

		Cursor cursor = Cursor.of(before, after);

		CaseInstanceFilter filter = new CaseInstanceFilter(status, caseDefinitionId, cursor, sort, limit);

		PageResult<CaseInstance> data = caseInstanceService.find(filter);

		return ResponseEntity.ok(data.toJson());
	}

	@GetMapping(value = "/{businessKey}")
	public ResponseEntity<CaseInstance> get(@PathVariable final String businessKey) {
		try {
			return ResponseEntity.ok(caseInstanceService.get(businessKey));
		} catch (CaseInstanceNotFoundException e) {
			throw new ResourceNotFoundException(e.getMessage());
		}
	}

	@PostMapping
	public ResponseEntity<CaseInstance> save(@RequestBody final CaseInstance caseInstance) {
		return ResponseEntity.ok(caseInstanceService.createWithValues(caseInstance));
	}

	@PatchMapping(value = "/{businessKey}", consumes = "application/merge-patch+json")
	public ResponseEntity<Void> mergePatch(@PathVariable final String businessKey, @RequestBody String mergePatchJson) {

		CaseInstance mergePatch = gsonBuilder.create().fromJson(mergePatchJson, CaseInstance.class);

		try {
			caseInstanceService.patch(businessKey, mergePatch);
		} catch (CaseInstanceNotFoundException e) {
			throw new ResourceNotFoundException(e.getMessage());
		}
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping(value = "/{businessKey}")
	public ResponseEntity<Void> delete(@PathVariable final String businessKey) {
		try {
			caseInstanceService.delete(businessKey);
		} catch (CaseInstanceNotFoundException e) {
			throw new ResourceNotFoundException(e.getMessage());
		}
		return ResponseEntity.noContent().build();
	}

	@PostMapping(value = "/{businessKey}/document")
	public ResponseEntity<Void> saveDocument(@PathVariable final String businessKey,
			@RequestBody CaseDocument document) {

		try {
			caseInstanceService.saveDocument(businessKey, document);
		} catch (CaseInstanceNotFoundException e) {
			throw new ResourceNotFoundException(e.getMessage());
		}
		return ResponseEntity.noContent().build();
	}

	@PostMapping(value = "/{businessKey}/comment")
	public ResponseEntity<Void> saveComment(@PathVariable final String businessKey,
			@RequestBody final CaseComment newComment) {

		try {
			caseInstanceService.saveComment(businessKey, newComment);
		} catch (CaseInstanceNotFoundException e) {
			throw new ResourceNotFoundException(e.getMessage());
		}
		return ResponseEntity.noContent().build();
	}

	@PutMapping(value = "/{businessKey}/comment/{commentId}")
	public ResponseEntity<Void> udpateComment(@PathVariable final String businessKey,
			@PathVariable final String commentId, @RequestBody final CaseComment comment) {

		try {
			caseInstanceService.updateComment(businessKey, commentId, comment.getBody());
		} catch (CaseInstanceNotFoundException e) {
			throw new ResourceNotFoundException(e.getMessage());
		}
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping(value = "/{businessKey}/comment/{commentId}")
	public ResponseEntity<Void> deleteComment(@PathVariable final String businessKey,
			@PathVariable final String commentId) {

		try {
			caseInstanceService.deleteComment(businessKey, commentId);
		} catch (CaseInstanceNotFoundException e) {
			throw new ResourceNotFoundException(e.getMessage());
		}
		return ResponseEntity.noContent().build();
	}

}
