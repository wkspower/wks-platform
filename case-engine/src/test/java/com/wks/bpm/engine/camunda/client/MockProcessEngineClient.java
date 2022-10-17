package com.wks.bpm.engine.camunda.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import com.wks.bpm.engine.BpmEngine;
import com.wks.bpm.engine.client.ProcessEngineClient;
import com.wks.bpm.engine.model.spi.ActivityInstance;
import com.wks.bpm.engine.model.spi.Deployment;
import com.wks.bpm.engine.model.spi.ProcessDefinition;
import com.wks.bpm.engine.model.spi.ProcessInstance;
import com.wks.bpm.engine.model.spi.ProcessMessage;
import com.wks.bpm.engine.model.spi.Task;
import com.wks.bpm.engine.model.spi.TaskForm;

public class MockProcessEngineClient implements ProcessEngineClient {

	private List<ProcessInstance> processesInstances = new ArrayList<>();

	@Override
	public Deployment[] findDeployments(final BpmEngine bpmEngine) {
		return null;
	}

	@Override
	public ProcessDefinition[] findProcessDefinitions(final BpmEngine bpmEngine) {
		return null;
	}

	@Override
	public ProcessInstance[] findProcessInstances(Optional<String> businessKey, final BpmEngine bpmEngine) {
		return processesInstances.stream().filter(o -> businessKey.get().equals(o.getBusinessKey()))
				.collect(Collectors.toList()).toArray(ProcessInstance[]::new);

	}

	@Override
	public ProcessInstance startProcess(String processDefinitionKey, final BpmEngine bpmEngine) {
		return null;
	}

	@Override
	public ProcessInstance startProcess(String processDefinitionKey, String businessKey, final BpmEngine bpmEngine) {
		ProcessInstance processInstance = ProcessInstance.builder().businessKey(businessKey).build();
		processesInstances.add(processInstance);
		return processInstance;
	}

	@Override
	public void deleteProcessInstance(String processInstanceId, final BpmEngine bpmEngine) {

	}

	@Override
	public Task[] findTasks(String processInstanceBusinessKey, final BpmEngine bpmEngine) {
		return null;
	}

	@Override
	public void claimTask(String taskId, String taskAssignee, final BpmEngine bpmEngine) {

	}

	@Override
	public void unclaimTask(String taskId, final BpmEngine bpmEngine) {

	}

	@Override
	public void complete(String taskId, JsonObject variables, final BpmEngine bpmEngine) {

	}

	@Override
	public TaskForm getTaskForm(String taskId, final BpmEngine bpmEngine) {
		return null;
	}

	@Override
	public String findVariables(String processInstanceId, final BpmEngine bpmEngine) {
		return null;
	}

	@Override
	public void sendMessage(ProcessMessage processMesage, final BpmEngine bpmEngine) {
	}

	@Override
	public ActivityInstance[] findActivityInstances(String processInstanceId, final BpmEngine bpmEngine) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProcessDefinitionXML(String processDefinitionId, final BpmEngine bpmEngine) {
		// TODO Auto-generated method stub
		return null;
	}
}
