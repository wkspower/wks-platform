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
package com.wks.bpm.engine.client.facade;

import java.util.List;
import java.util.Optional;

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
public interface BpmEngineClientFacade {

	void deploy(final String fileName, final String bpmnXml);

	Deployment[] findDeployments();

	ProcessDefinition[] findProcessDefinitions();

	String getProcessDefinitionXMLById(final String processDefinitionId) throws ProcessDefinitionNotFoundException;

	String getProcessDefinitionXMLByKey(final String processDefinitionKey) throws ProcessDefinitionNotFoundException;

	ProcessInstance[] findProcessInstances(final Optional<String> processDefinitionKey,
			final Optional<String> businessKey, final Optional<String> activityIdIn);

	ProcessInstance startProcess(final String processDefinitionKey, final Optional<String> businessKey,
			final Optional<ProcessVariable> processVariable);

	ProcessInstance startProcess(final String processDefinitionKey, final Optional<String> businessKey,
			final List<ProcessVariable> processVariables);

	void deleteProcessInstance(String processInstanceId);

	ActivityInstance[] findActivityInstances(String processInstanceId) throws ProcessInstanceNotFoundException;

	void createTask(final Task task);

	Task getTask(final String taskId);

	Task[] findTasks(final String processInstanceBusinessKey);

	void claimTask(String taskId, String taskAssignee);

	void unclaimTask(String taskId);

	void complete(String taskId, List<ProcessVariable> variables);

	ProcessVariable[] findVariables(String processInstanceId);

	void sendMessage(ProcessMessage processMesage, Optional<List<ProcessVariable>> variables);

}
