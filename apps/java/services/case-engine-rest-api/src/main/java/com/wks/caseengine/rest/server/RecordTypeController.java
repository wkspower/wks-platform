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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.record.type.RecordType;
import com.wks.caseengine.record.type.RecordTypeNotFoundException;
import com.wks.caseengine.record.type.RecordTypeService;
import com.wks.caseengine.rest.exception.ResourceNotFoundException;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("record-type")
@Tag(name = "Record Type")
public class RecordTypeController {

	@Autowired
	private RecordTypeService recordTypeService;

	@GetMapping
	public ResponseEntity<List<RecordType>> find() {
		return ResponseEntity.ok(recordTypeService.find());
	}

	@GetMapping(value = "/{id}")
	public ResponseEntity<RecordType> get(@PathVariable final String id) {
		try {
			return ResponseEntity.ok(recordTypeService.get(id));
		} catch (RecordTypeNotFoundException e) {
			throw new ResourceNotFoundException(e.getMessage());
		}
	}

	@PostMapping
	public ResponseEntity<Void> save(@RequestBody final RecordType recordType) {
		recordTypeService.save(recordType);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping(value = "/{id}")
	public ResponseEntity<Void> delete(@PathVariable final String id) {
		try {
			recordTypeService.delete(id);
		} catch (RecordTypeNotFoundException e) {
			throw new ResourceNotFoundException(e.getMessage());
		}
		return ResponseEntity.noContent().build();
	}

	@PatchMapping(value = "/{id}")
	public ResponseEntity<Void> update(@PathVariable final String id, @RequestBody final RecordType recordType) {
		try {
			recordTypeService.update(id, recordType);
		} catch (RecordTypeNotFoundException e) {
			throw new ResourceNotFoundException(e.getMessage());
		}

		return ResponseEntity.noContent().build();
	}

}
