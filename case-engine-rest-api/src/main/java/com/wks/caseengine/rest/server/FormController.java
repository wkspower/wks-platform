package com.wks.caseengine.rest.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.wks.bpm.engine.model.spi.Form;
import com.wks.caseengine.client.forms.FormService;

@RestController
public class FormController {

	@Autowired
	private FormService formService;

	@GetMapping(value = "/form/{taskId}")
	public Form find(@PathVariable String taskId) {
		return formService.getTaskForm(taskId);
	}

}
