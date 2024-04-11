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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.bpm.engine.BpmEngine;
import com.wks.bpm.engine.BpmEngineType;
import com.wks.bpm.engine.client.BpmEngineClient;
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
@Component
public class DefaultBpmEngineClientFacade implements BpmEngineClientFacade {

	@Autowired
	private BpmEngineClient engineClient;

	private BpmEngine bpmEngine = new DefaultC7BpmEngine();

	private BpmEngine getBpmEngine() {
		return bpmEngine;
	}

	private BpmEngineClient getEngineClient() {
		return engineClient;
	}

	@Override
	public void deploy(final String fileName, final String bpmnXml) {
		getEngineClient().deploy(getBpmEngine(), fileName, bpmnXml);
	}

	@Override
	public Deployment[] findDeployments() {
		return getEngineClient().findDeployments(getBpmEngine());
	}

	@Override
	public ProcessDefinition[] findProcessDefinitions() {
		return getEngineClient().findProcessDefinitions(getBpmEngine());
	}

	@Override
	public String getProcessDefinitionXMLById(final String processDefinitionId)
			throws ProcessDefinitionNotFoundException {
		return getEngineClient().getProcessDefinitionXMLById(processDefinitionId, getBpmEngine());
	}

	@Override
	public String getProcessDefinitionXMLByKey(final String processDefinitionKey)
			throws ProcessDefinitionNotFoundException {
		return getEngineClient().getProcessDefinitionXMLByKey(processDefinitionKey, getBpmEngine());
	}

	@Override
	public ProcessInstance[] findProcessInstances(final Optional<String> processDefinitionKey,
			final Optional<String> businessKey, final Optional<String> activityIdIn) {
		return getEngineClient().findProcessInstances(processDefinitionKey, businessKey, activityIdIn, getBpmEngine());
	}

	@Override
	public ProcessInstance startProcess(final String processDefinitionKey, final Optional<String> businessKey,
			final Optional<ProcessVariable> variable) {
		return getEngineClient().startProcess(processDefinitionKey, businessKey, variable, getBpmEngine());
	}

	@Override
	public ProcessInstance startProcess(final String processDefinitionKey, final Optional<String> businessKey,
			final List<ProcessVariable> variables) {
		return getEngineClient().startProcess(processDefinitionKey, businessKey, variables, getBpmEngine());
	}

	@Override
	public void deleteProcessInstance(String processInstanceId) {
		getEngineClient().deleteProcessInstance(processInstanceId, getBpmEngine());
	}

	@Override
	public ActivityInstance[] findActivityInstances(String processInstanceId) throws ProcessInstanceNotFoundException {
		return getEngineClient().findActivityInstances(processInstanceId, getBpmEngine());
	}

	@Override
	public void createTask(Task task) {
		getEngineClient().createTask(task, getBpmEngine());
	}

	@Override
	public Task getTask(final String taskId) {
		return getEngineClient().getTask(taskId, getBpmEngine());
	}

	@Override
	public Task[] findTasks(final Optional<String> processInstanceBusinessKey) {
		return getEngineClient().findTasks(processInstanceBusinessKey, getBpmEngine());
	}

	@Override
	public void claimTask(String taskId, String taskAssignee) {
		getEngineClient().claimTask(taskId, taskAssignee, getBpmEngine());
	}

	@Override
	public void unclaimTask(String taskId) {
		getEngineClient().unclaimTask(taskId, getBpmEngine());
	}

	@Override
	public void complete(String taskId, List<ProcessVariable> variables) {
		getEngineClient().complete(taskId, variables, getBpmEngine());
	}

	@Override
	public ProcessVariable[] findVariables(String processInstanceId) {
		return getEngineClient().findVariables(processInstanceId, getBpmEngine());
	}

	@Override
	public void sendMessage(ProcessMessage processMesage, Optional<List<ProcessVariable>> correlateKeys) {
		getEngineClient().sendMessage(processMesage, correlateKeys, getBpmEngine());
	}

	public class DefaultC7BpmEngine extends BpmEngine {

		private String DEFAULT_C7_BPM_ID = "default-camunda7-engine";
		private String DEFAULT_C7_BPM_NAME = "Default Camunda 7";

		@Override
		public String getId() {
			return DEFAULT_C7_BPM_ID;
		}

		@Override
		public String getName() {
			return DEFAULT_C7_BPM_NAME;
		}

		@Override
		public BpmEngineType getType() {
			return BpmEngineType.BPM_ENGINE_CAMUNDA7;
		}

	}

}
