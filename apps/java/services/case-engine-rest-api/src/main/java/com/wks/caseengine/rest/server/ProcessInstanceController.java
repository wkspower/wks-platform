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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.bpm.engine.model.spi.ActivityInstance;
import com.wks.bpm.engine.model.spi.ProcessInstance;
import com.wks.caseengine.process.instance.ProcessInstanceService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("process-instance")
@Tag(name = "Process Instance", description = "Access information about processes instances in Camunda")
public class ProcessInstanceController {

	@Autowired
	private ProcessInstanceService processInstanceService;
	
	@GetMapping
	public ResponseEntity<List<ProcessInstance>> find(@RequestParam(required = false) String businessKey) {
		return ResponseEntity
				.ok(processInstanceService.find(Optional.empty(), Optional.ofNullable(businessKey), Optional.empty()));
	}

	@GetMapping(value = "/{id}/activity-instances")
	public ResponseEntity<List<ActivityInstance>> getActivityInstances(@PathVariable final String id) {
		return ResponseEntity.ok(processInstanceService.getActivityInstances(id));
	}

}
