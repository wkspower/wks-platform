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

@RestController
@RequestMapping("task")
public class TaskController {

	@Autowired
	private TaskService taskService;

	@GetMapping(value = "/")
	public List<Task> find(@RequestParam(required = false) String processInstanceBusinessKey) {
		return taskService.find(processInstanceBusinessKey);
	}

	@PostMapping(value = "/{taskId}/claim/{taskAssignee}")
	public void claim(@PathVariable String taskId, @PathVariable String taskAssignee) {
		taskService.claim(taskId, taskAssignee);
	}

	@PostMapping(value = "/{taskId}/unclaim")
	public void unclaim(@PathVariable String taskId) {
		taskService.unclaim(taskId);
	}

	@PostMapping(value = "/{taskId}/complete")
	public void complete(@PathVariable String taskId, @RequestBody String variables) {
		taskService.complete(taskId, JsonParser.parseString(variables).getAsJsonObject());
	}

}
