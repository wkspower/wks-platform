package com.wks.caseengine.client.forms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.bpm.engine.camunda.client.ProcessEngineClient;
import com.wks.bpm.engine.model.spi.Form;

@Component
public class FormServiceImpl implements FormService {

	@Autowired
	private ProcessEngineClient processEngineClient;

	@Override
	public Form getTaskForm(String taskId) {
		return processEngineClient.getTaskForm(taskId);
	}

}
