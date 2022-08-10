package com.mmc.bpm.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mmc.bpm.client.tasks.TaskService;
import com.mmc.bpm.engine.model.spi.Task;
import com.mmc.bpm.engine.model.spi.TaskAssignee;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
public class TaskController {

	@Autowired
	private TaskService taskService;

	@GetMapping(value = "/task")
	public List<Task> find() {
		return taskService.find();
	}

	@PostMapping(value = "/task/{taskId}/claim")
	public void claim(@PathVariable String taskId, @RequestBody TaskAssignee taskAssignee) {
		taskService.claim(taskId, taskAssignee);
	}

	@PostMapping(value = "/task/{taskId}/unclaim")
	public void unclaim(@PathVariable String taskId) {
		taskService.unclaim(taskId);
	}

}
