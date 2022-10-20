package com.wks.caseengine.tasks.forms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.bpm.engine.client.BpmEngineClientFacade;
import com.wks.bpm.engine.model.spi.TaskForm;
import com.wks.caseengine.repository.BpmEngineRepository;

@Component
public class TaskFormServiceImpl implements TaskFormService {

	@Autowired
	private BpmEngineClientFacade processEngineClient;

	@Autowired
	private BpmEngineRepository bpmEngineRepository;

	@Override
	public TaskForm get(final String taskId, final String bpmEngineId) throws Exception {
		return processEngineClient.getTaskForm(taskId, bpmEngineRepository.get(bpmEngineId));
	}

}
