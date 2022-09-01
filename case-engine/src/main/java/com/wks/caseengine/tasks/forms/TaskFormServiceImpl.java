package com.wks.caseengine.tasks.forms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.bpm.engine.camunda.client.ProcessEngineClient;
import com.wks.bpm.engine.model.spi.TaskForm;

@Component
public class TaskFormServiceImpl implements FormTaskService {

	@Autowired
	private ProcessEngineClient processEngineClient;

	@Override
	public TaskForm getTaskForm(String taskId) {
		return processEngineClient.getTaskForm(taskId);
	}

}
