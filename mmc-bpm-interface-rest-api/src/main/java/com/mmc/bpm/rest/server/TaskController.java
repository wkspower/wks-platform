package com.mmc.bpm.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mmc.bpm.client.tasks.TaskService;
import com.mmc.bpm.engine.model.spi.Task;

@RestController
public class TaskController {

	@Autowired
	private TaskService taskService;

	@GetMapping(value = "/task")
	public List<Task> find() {

		return taskService.find();
	}

}
