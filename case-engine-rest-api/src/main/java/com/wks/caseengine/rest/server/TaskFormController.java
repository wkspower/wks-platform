package com.wks.caseengine.rest.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.bpm.engine.model.spi.TaskForm;
import com.wks.caseengine.tasks.forms.TaskFormService;

@RestController
@RequestMapping("task-form")
public class TaskFormController {

	@Autowired
	private TaskFormService formService;

	@GetMapping(value = "/{bpmEngineId}/{taskId}")
	public TaskForm find(final @PathVariable String bpmEngineId, @PathVariable String taskId) throws Exception {
		return formService.get(taskId, bpmEngineId);
	}

}
