package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonParser;
import com.wks.bpm.engine.model.spi.Task;
import com.wks.caseengine.tasks.TaskService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("task")
@Tag(name = "Task", description = "A task is a unit of work for a process")
public class TaskController {

	@Autowired
	private TaskService taskService;

	@GetMapping(value = "/")
	public List<Task> find(@RequestParam(required = false) String processInstanceBusinessKey) throws Exception {
		return taskService.find(processInstanceBusinessKey);
	}

	@PostMapping(value = "/{taskId}/claim/{taskAssignee}")
	public void claim(@PathVariable final String taskId, @PathVariable final String taskAssignee) throws Exception {
		taskService.claim(taskId, taskAssignee);
	}

	@PostMapping(value = "/{taskId}/unclaim")
	public void unclaim(@PathVariable final String taskId) throws Exception {
		taskService.unclaim(taskId);
	}

	@PostMapping(value = "/{taskId}/complete")
	public void complete(@PathVariable final String taskId, @RequestBody final String variables) throws Exception {
		taskService.complete(taskId, JsonParser.parseString(variables).getAsJsonObject());
	}

}
