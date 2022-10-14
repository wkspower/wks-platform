package com.wks.caseengine.rest.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.bpm.engine.model.spi.TaskForm;
import com.wks.caseengine.tasks.forms.FormTaskService;

@RestController
@RequestMapping("task-form")
public class TaskFormController {

	@Autowired
	private FormTaskService formService;

	@GetMapping(value = "/{taskId}")
	public TaskForm find(@PathVariable String taskId) {
		return formService.getTaskForm(taskId);
	}

}
