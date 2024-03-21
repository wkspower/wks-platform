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
package com.wks.bpm.engine.client;

import java.util.List;
import java.util.Optional;

import com.wks.bpm.engine.BpmEngine;
import com.wks.bpm.engine.exception.ProcessDefinitionNotFoundException;
import com.wks.bpm.engine.exception.ProcessInstanceNotFoundException;
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
public interface BpmEngineClient {

	void deploy(final BpmEngine bpmEngine, final String fileName, final String bpmnXml);

	Deployment[] findDeployments(final BpmEngine bpmEngine);

	ProcessDefinition[] findProcessDefinitions(final BpmEngine bpmEngine);

	ProcessInstance[] findProcessInstances(final Optional<String> processDefinitionKey,
			final Optional<String> businessKey, final Optional<String> activityIdIn, final BpmEngine bpmEngine);

	String getProcessDefinitionXMLById(final String processDefinitionId, final BpmEngine bpmEngine)
			throws ProcessDefinitionNotFoundException;

	String getProcessDefinitionXMLByKey(final String processDefinitionKey, final BpmEngine bpmEngine)
			throws ProcessDefinitionNotFoundException;

	public ProcessInstance startProcess(final String processDefinitionKey, final Optional<String> businessKey,
			final Optional<ProcessVariable> processVariable, final BpmEngine bpmEngine);

	public ProcessInstance startProcess(final String processDefinitionKey, final Optional<String> businessKey,
			final List<ProcessVariable> processVariables, final BpmEngine bpmEngine);

	void deleteProcessInstance(final String processInstanceId, final BpmEngine bpmEngine);

	ActivityInstance[] findActivityInstances(final String processInstanceId, final BpmEngine bpmEngine)
			throws ProcessInstanceNotFoundException;

	public void createTask(final Task task, final BpmEngine bpmEngine);

	public Task getTask(final String taskId, final BpmEngine bpmEngine);

	Task[] findTasks(final Optional<String> processInstanceBusinessKey, final BpmEngine bpmEngine);

	void claimTask(String taskId, String taskAssignee, final BpmEngine bpmEngine);

	void unclaimTask(String taskId, final BpmEngine bpmEngine);

	void complete(String taskId, List<ProcessVariable> variables, final BpmEngine bpmEngine);

	ProcessVariable[] findVariables(final String processInstanceId, final BpmEngine bpmEngine);

	void sendMessage(final ProcessMessage processMesage, final Optional<List<ProcessVariable>> variables,
			final BpmEngine bpmEngine);

}
