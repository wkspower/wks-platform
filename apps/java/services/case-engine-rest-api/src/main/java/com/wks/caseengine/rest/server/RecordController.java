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
import org.springframework.web.client.ResourceAccessException;

import com.google.gson.JsonObject;
import com.wks.caseengine.record.RecordNotFoundException;
import com.wks.caseengine.record.RecordService;
import com.wks.caseengine.exception.RestResourceNotFoundException;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("record")
@Tag(name = "Record")
public class RecordController {

	@Autowired
	private RecordService recordService;

	@GetMapping(value = "/{recordTypeId}")
	public ResponseEntity<List<JsonObject>> find(@PathVariable final String recordTypeId) {
		return ResponseEntity.ok(recordService.find(recordTypeId));
	}

	@GetMapping(value = "/{recordTypeId}/{id}")
	public ResponseEntity<JsonObject> get(@PathVariable final String recordTypeId, @PathVariable final String id) {
		try {
			return ResponseEntity.ok(recordService.get(recordTypeId, id));
		} catch (RecordNotFoundException e) {
			throw new ResourceAccessException(e.getMessage());
		}
	}

	@PostMapping(value = "/{recordTypeId}")
	public ResponseEntity<Void> save(@PathVariable final String recordTypeId, @RequestBody final JsonObject record) {
		recordService.save(recordTypeId, record);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping(value = "/{recordTypeId}/{id}")
	public ResponseEntity<Void> delete(@PathVariable final String recordTypeId, @PathVariable final String id) {
		try {
			recordService.delete(recordTypeId, id);
		} catch (RecordNotFoundException e) {
			throw new RestResourceNotFoundException(e.getMessage());
		}
		return ResponseEntity.noContent().build();
	}

	@PatchMapping(value = "/{recordTypeId}/{id}")
	public ResponseEntity<Void> update(@PathVariable final String recordTypeId, @PathVariable final String id,
			@RequestBody final JsonObject record) {
		try {
			recordService.update(recordTypeId, id, record);
		} catch (RestResourceNotFoundException e) {
			throw new RestResourceNotFoundException(e.getMessage());
		}
		return ResponseEntity.noContent().build();
	}

}
