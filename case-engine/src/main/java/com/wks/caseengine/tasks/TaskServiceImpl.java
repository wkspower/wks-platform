package com.wks.caseengine.tasks;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.wks.bpm.engine.client.BpmEngineClientFacade;
import com.wks.bpm.engine.model.spi.Task;

@Component
public class TaskServiceImpl implements TaskService {

	@Autowired
	private BpmEngineClientFacade processEngineClient;

	@Override
	public List<Task> find(final String processInstanceBusinessKey) throws Exception {
		return Arrays.asList(processEngineClient.findTasks(processInstanceBusinessKey));
	}

	@Override
	public void claim(final String taskId, final String taskAssignee) throws Exception {
		processEngineClient.claimTask(taskId, taskAssignee);

	}

	@Override
	public void unclaim(final String taskId) throws Exception {
		processEngineClient.unclaimTask(taskId);
	}

	@Override
	public void complete(final String taskId, final JsonObject variables) throws Exception {
		processEngineClient.complete(taskId, variables);
	}

}
