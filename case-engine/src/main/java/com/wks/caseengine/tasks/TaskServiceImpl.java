package com.wks.caseengine.tasks;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.wks.bpm.engine.client.BpmEngineClientFacade;
import com.wks.bpm.engine.model.spi.Task;
import com.wks.caseengine.tasks.event.complete.TaskCompleteEvent;
import com.wks.caseengine.tasks.event.complete.TaskCompleteEventObject;

@Component
public class TaskServiceImpl implements TaskService {

	@Autowired
	private BpmEngineClientFacade processEngineClient;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	
	@Override
	public void create(Task task) throws Exception {
		processEngineClient.createTask(task);
	}

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
		Task task = processEngineClient.getTask(taskId);
		processEngineClient.complete(taskId, variables);

		applicationEventPublisher
				.publishEvent(new TaskCompleteEvent(new TaskCompleteEventObject(task.getProcessDefinitionId(),
						task.getTaskDefinitionKey(), task.getCaseInstanceId())));
	}

}
