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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.google.gson.GsonBuilder;
import com.wks.caseengine.cases.instance.CaseDocument;
import com.wks.caseengine.cases.instance.CaseFilter;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.CaseInstanceNotFoundException;
import com.wks.caseengine.cases.instance.Comment;
import com.wks.caseengine.cases.instance.service.CaseInstanceService;
import com.wks.caseengine.pagination.Cursor;
import com.wks.caseengine.pagination.PageResult;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("case")
@Tag(name = "Case Instance", description = "A Case Instance is created based in a Case Definition and is the 'Digital Folder' for related information, documents, communication and processes for case")
public class CaseController {

	@Autowired
	private CaseInstanceService caseInstanceService;

	@Autowired
	private GsonBuilder gsonBuilder;

	@GetMapping(value = "/")
	public ResponseEntity<Object> find(@RequestParam(required = false) String status,
			@RequestParam(required = false) String caseDefinitionId,
			@RequestParam(required = false, name = "before") String before,
			@RequestParam(required = false, name = "after") String after,
			@RequestParam(required = false, name = "sort") String sort,
			@RequestParam(required = false, name = "limit") String limit) throws Exception {
		Cursor cursor = Cursor.of(before, after);

		CaseFilter filter = new CaseFilter(status, caseDefinitionId, cursor, sort, limit);

		PageResult<CaseInstance> data = caseInstanceService.find(filter);

		return new ResponseEntity<>(data.toJson(), HttpStatus.OK);
	}

	@GetMapping(value = "/{businessKey}")
	public CaseInstance get(@PathVariable final String businessKey) throws Exception {
		return caseInstanceService.get(businessKey);
	}

	@PostMapping(value = "/")
	public CaseInstance save(@RequestBody final CaseInstance caseInstance) throws Exception {
		return caseInstanceService.create(caseInstance);
	}

	@PatchMapping(value = "/{businessKey}", consumes = "application/merge-patch+json")
	public ResponseEntity<Void> mergePatch(@PathVariable final String businessKey, @RequestBody String mergePatchJson)
			throws Exception {

		CaseInstance mergePatch = gsonBuilder.create().fromJson(mergePatchJson, CaseInstance.class);

		caseInstanceService.patch(businessKey, mergePatch);
		
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping(value = "/{businessKey}")
	public ResponseEntity<Void> delete(@PathVariable final String businessKey) throws Exception {
		try {
			caseInstanceService.delete(businessKey);
			return ResponseEntity.noContent().build();
		} catch (CaseInstanceNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Case Instance Not Found - " + businessKey, e);
		}
	}

	@PostMapping(value = "/{businessKey}/document")
	public ResponseEntity<Void> saveDocument(@PathVariable final String businessKey, @RequestBody CaseDocument document)
			throws Exception {
		caseInstanceService.saveDocument(businessKey, document);
		return ResponseEntity.noContent().build();
	}

	@PostMapping(value = "/{businessKey}/comment")
	public ResponseEntity<Void> saveComment(@PathVariable final String businessKey, @RequestBody final Comment newComment)
			throws Exception {
		caseInstanceService.saveComment(businessKey, newComment);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping(value = "/{businessKey}/comment/{commentId}")
	public ResponseEntity<Void> udpateComment(@PathVariable final String businessKey, @PathVariable final String commentId,
			@RequestBody final Comment comment) throws Exception {
		caseInstanceService.updateComment(businessKey, commentId, comment.getBody());
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping(value = "/{businessKey}/comment/{commentId}")
	public ResponseEntity<Void> deleteComment(@PathVariable final String businessKey, @PathVariable final String commentId)
			throws Exception {
		caseInstanceService.deleteComment(businessKey, commentId);
		return ResponseEntity.noContent().build();
	}

}
