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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.bpm.engine.model.spi.ProcessVariable;
import com.wks.bpm.engine.model.spi.Task;
import com.wks.caseengine.tasks.TaskService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("task")
@Tag(name = "Task", description = "A task is a unit of work for a process")
public class TaskController {

	@Autowired
	private TaskService taskService;

	@PostMapping(value = "/create")
	public ResponseEntity<Void> create(@RequestBody final Task task) {
		taskService.create(task);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	public ResponseEntity<List<Task>> find(@RequestParam(required = false) String businessKey) {
		return ResponseEntity.ok(taskService.find(Optional.ofNullable(businessKey)));
	}

	@PostMapping(value = "/{taskId}/claim/{taskAssignee}")
	public ResponseEntity<Void> claim(@PathVariable final String taskId, @PathVariable final String taskAssignee) {
		taskService.claim(taskId, taskAssignee);
		return ResponseEntity.noContent().build();
	}

	@PostMapping(value = "/{taskId}/unclaim")
	public ResponseEntity<Void> unclaim(@PathVariable final String taskId) {
		taskService.unclaim(taskId);
		return ResponseEntity.noContent().build();
	}

	@PostMapping(value = "/{taskId}/complete")
	public ResponseEntity<Void> complete(@PathVariable final String taskId,
			@RequestBody final List<ProcessVariable> variables) {
		taskService.complete(taskId, variables);
		return ResponseEntity.noContent().build();
	}

}
