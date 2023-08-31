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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;
import com.wks.caseengine.record.RecordService;

@RestController
@RequestMapping("record")
public class RecordController {

	@Autowired
	private RecordService recordService;

	@GetMapping(value = "/{recordTypeId}")
	public List<JsonObject> find(@PathVariable final String recordTypeId) throws Exception {
		return recordService.find(recordTypeId);
	}

	@GetMapping(value = "/{recordTypeId}/{id}")
	public JsonObject get(@PathVariable final String recordTypeId, @PathVariable final String id) throws Exception {
		return recordService.get(recordTypeId, id);
	}

	@PostMapping(value = "/{recordTypeId}")
	public void save(@PathVariable final String recordTypeId, @RequestBody final JsonObject record) throws Exception {
		recordService.save(recordTypeId, record);
	}

	@DeleteMapping(value = "/{recordTypeId}/{id}")
	public void delete(@PathVariable final String recordTypeId, @PathVariable final String id) throws Exception {
		recordService.delete(recordTypeId, id);
	}

	@PatchMapping(value = "/{recordTypeId}/{id}")
	public void update(@PathVariable final String recordTypeId, @PathVariable final String id,
			@RequestBody final JsonObject record) throws Exception {
		recordService.update(recordTypeId, id, record);
	}

}
