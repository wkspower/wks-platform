package com.wks.bpm.engine.camunda.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
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
	public Deployment[] findDeployments() {
		return null;
	}

	@Override
	public ProcessDefinition[] findProcessDefinitions() {
		return null;
	}

	@Override
	public ProcessInstance[] findProcessInstances(Optional<String> businessKey) {
		return processesInstances.stream().filter(o -> businessKey.get().equals(o.getBusinessKey()))
				.collect(Collectors.toList()).toArray(ProcessInstance[]::new);

	}

	@Override
	public ProcessInstance startProcess(String processDefinitionKey) {
		return null;
	}

	@Override
	public ProcessInstance startProcess(String processDefinitionKey, String businessKey) {
		ProcessInstance processInstance = ProcessInstance.builder().businessKey(businessKey).build();
		processesInstances.add(processInstance);
		return processInstance;
	}

	@Override
	public void deleteProcessInstance(String processInstanceId) {

	}

	@Override
	public Task[] findTasks(String processInstanceBusinessKey) {
		return null;
	}

	@Override
	public void claimTask(String taskId, String taskAssignee) {

	}

	@Override
	public void unclaimTask(String taskId) {

	}

	@Override
	public void complete(String taskId, JsonObject variables) {

	}

	@Override
	public TaskForm getTaskForm(String taskId) {
		return null;
	}

	@Override
	public String findVariables(String processInstanceId) {
		return null;
	}

	@Override
	public void sendMessage(ProcessMessage processMesage) {
	}

	@Override
	public ActivityInstance[] findActivityInstances(String processInstanceId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProcessDefinitionXML(String processDefinitionId) {
		// TODO Auto-generated method stub
		return null;
	}
}
