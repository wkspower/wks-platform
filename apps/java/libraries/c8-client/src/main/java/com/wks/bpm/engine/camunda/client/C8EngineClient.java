/*
 * WKS Platform - Open-Source Project
 * 
 * This file is part of the WKS Platform, an open-source project developed by WKS Power.
 * 
 * WKS Platform is licensed under the MIT License.
 * 
 * © 2021 WKS Power. All rights reserved.
 * 
 * For licensing information, see the LICENSE file in the root directory of the project.
 */
package com.wks.bpm.engine.camunda.client;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.wks.bpm.engine.BpmEngine;
import com.wks.bpm.engine.client.BpmEngineClient;
import com.wks.bpm.engine.model.spi.ActivityInstance;
import com.wks.bpm.engine.model.spi.Deployment;
import com.wks.bpm.engine.model.spi.ProcessDefinition;
import com.wks.bpm.engine.model.spi.ProcessInstance;
import com.wks.bpm.engine.model.spi.ProcessMessage;
import com.wks.bpm.engine.model.spi.ProcessVariable;
import com.wks.bpm.engine.model.spi.Task;

/**
 * @author victor.franca
 *
 */
@Component
@Qualifier("c8EngineClient")
public class C8EngineClient implements BpmEngineClient {

	@Autowired
	private C8ZeebeClient zeebeClient;

	@Autowired
	private C8OperateClient operateClient;

	@Autowired
	private C8TasklistClient tasklistClient;

	@Override
	public void deploy(final BpmEngine bpmEngine, final String fileName, final String bpmnXml) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Deployment[] findDeployments(final BpmEngine bpmEngine) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public ProcessDefinition[] findProcessDefinitions(final BpmEngine bpmEngine) {
		return operateClient.findProcessDefinitions(bpmEngine);
	}

	@Override
	public ProcessInstance[] findProcessInstances(final Optional<String> processDefinitionKey,
			final Optional<String> businessKey, final Optional<String> activityIdIn, final BpmEngine bpmEngine) {
		return operateClient.searchProcessInstances(processDefinitionKey, businessKey, activityIdIn, bpmEngine);
	}

	@Override
	public String getProcessDefinitionXMLById(final String processDefinitionId, final BpmEngine bpmEngine) {
		return operateClient.getProcessDefinitionXMLById(processDefinitionId, bpmEngine);
	}

	@Override
	public String getProcessDefinitionXMLByKey(final String processDefinitionKey, final BpmEngine bpmEngine) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ProcessInstance startProcess(final String processDefinitionKey, final Optional<String> businessKey,
			Optional<ProcessVariable> processVariable, BpmEngine bpmEngine) {
		return zeebeClient.startProcess(processDefinitionKey, businessKey, processVariable,
				bpmEngine);
	}

	@Override
	public ProcessInstance startProcess(final String processDefinitionKey, final Optional<String> businessKey,
			List<ProcessVariable> processVariables, BpmEngine bpmEngine) {
		return zeebeClient.startProcess(processDefinitionKey, businessKey, processVariables, bpmEngine);
	}

	@Override
	public void deleteProcessInstance(final String processInstanceId, final BpmEngine bpmEngine) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ActivityInstance[] findActivityInstances(final String processInstanceId, final BpmEngine bpmEngine) {
		return new ActivityInstance[0];
	}

	@Override
	public void createTask(Task task, BpmEngine bpmEngine) {
		throw new UnsupportedOperationException();

	}

	@Override
	public Task getTask(final String taskId, final BpmEngine bpmEngine) {
		return tasklistClient.getTask(taskId, bpmEngine);
	}

	@Override
	public Task[] findTasks(final String processInstanceBusinessKey, final BpmEngine bpmEngine) {
		ProcessInstance[] processInstances = operateClient.searchProcessInstances(Optional.empty(),
				Optional.of(processInstanceBusinessKey), Optional.empty(), bpmEngine);

		return processInstances.length > 0 ? tasklistClient.find(processInstances[0].getId(), bpmEngine) : new Task[0];
	}

	@Override
	public void claimTask(final String taskId, final String taskAssignee, final BpmEngine bpmEngine) {
		tasklistClient.claimTask(taskId, taskAssignee, bpmEngine);
	}

	@Override
	public void unclaimTask(final String taskId, final BpmEngine bpmEngine) {
		tasklistClient.unclaimTask(taskId, bpmEngine);
	}

	@Override
	public void complete(final String taskId, final List<ProcessVariable> variables, final BpmEngine bpmEngine) {
		C8Task task = tasklistClient.complete(taskId, bpmEngine);
		zeebeClient.setVariables(task.getProcessInstanceId(), variables);
	}

	@Override
	public ProcessVariable[] findVariables(final String processInstanceId, final BpmEngine bpmEngine) {
		return operateClient.findVariables(processInstanceId, bpmEngine);
	}

	@Override
	public void sendMessage(final ProcessMessage processMesage, final Optional<List<ProcessVariable>> variables,
			final BpmEngine bpmEngine) {
		throw new UnsupportedOperationException();
	}

}
