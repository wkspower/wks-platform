package com.mmc.bpm.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonParser;
import com.mmc.bpm.client.tasks.TaskService;
import com.mmc.bpm.engine.model.spi.Task;

@RestController
public class TaskController {

	@Autowired
	private TaskService taskService;

	@GetMapping(value = "/task")
	public List<Task> find(@RequestParam(required = false) String processInstanceBusinessKey) {
		return taskService.find(processInstanceBusinessKey);
	}

	@PostMapping(value = "/task/{taskId}/claim/{taskAssignee}")
	public void claim(@PathVariable String taskId, @PathVariable String taskAssignee) {
		taskService.claim(taskId, taskAssignee);
	}

	@PostMapping(value = "/task/{taskId}/unclaim")
	public void unclaim(@PathVariable String taskId) {
		taskService.unclaim(taskId);
	}

	@PostMapping(value = "/task/{taskId}/complete")
	public void complete(@PathVariable String taskId, @RequestBody String variables) {
		taskService.complete(taskId, JsonParser.parseString(variables).getAsJsonObject());
	}

}
