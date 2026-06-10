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
package com.wks.bpm.engine.noop.client;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.wks.bpm.engine.BpmEngine;
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

import lombok.extern.slf4j.Slf4j;

/**
 * No-op {@link BpmEngineClient} used when {@code wks.bpm.engine=none}, i.e. the
 * platform runs without a workflow engine (no Camunda). It satisfies the single
 * {@code BpmEngineClient} dependency of the facade so the context starts with no
 * Camunda beans.
 *
 * <p>It deliberately does NOT persist cases on {@code startProcess}: in workflow
 * mode the case is written by the {@code caseSave} external task, not by the BPM
 * client. With {@code wks.bpm.engine=none} the case is persisted directly by the
 * {@code DirectCasePersistenceStrategy}, so this client only no-ops the
 * orchestration calls. All other BPM operations (tasks, deployments, process
 * definitions/instances) return empty results.
 *
 * <p>Lives in its own {@code noop-bpm-engine-client} module (mirroring
 * {@code c7-client}) so the contract module {@code bpm-engine-client} carries no
 * concrete engine implementation. Discovered via {@code NoopBpmEngineClientScan}.
 */
@Slf4j
@Component
public class NoopBpmEngineClient implements BpmEngineClient {

	@Override
	public void deploy(final BpmEngine bpmEngine, final String fileName, final String bpmnXml) {
		log.debug("[bpm=none] deploy ignored: {}", fileName);
	}

	@Override
	public Deployment[] findDeployments(final BpmEngine bpmEngine) {
		return new Deployment[0];
	}

	@Override
	public ProcessDefinition[] findProcessDefinitions(final BpmEngine bpmEngine) {
		return new ProcessDefinition[0];
	}

	@Override
	public ProcessInstance[] findProcessInstances(final Optional<String> processDefinitionKey,
			final Optional<String> businessKey, final Optional<String> activityIdIn, final BpmEngine bpmEngine) {
		return new ProcessInstance[0];
	}

	@Override
	public String getProcessDefinitionXMLById(final String processDefinitionId, final BpmEngine bpmEngine)
			throws ProcessDefinitionNotFoundException {
		return null;
	}

	@Override
	public String getProcessDefinitionXMLByKey(final String processDefinitionKey, final BpmEngine bpmEngine)
			throws ProcessDefinitionNotFoundException {
		return null;
	}

	@Override
	public ProcessInstance startProcess(final String processDefinitionKey, final Optional<String> businessKey,
			final Optional<ProcessVariable> processVariable, final BpmEngine bpmEngine) {
		log.debug("[bpm=none] startProcess ignored for key {}", processDefinitionKey);
		return ProcessInstance.builder().businessKey(businessKey.orElse(null)).build();
	}

	@Override
	public ProcessInstance startProcess(final String processDefinitionKey, final Optional<String> businessKey,
			final List<ProcessVariable> processVariables, final BpmEngine bpmEngine) {
		log.debug("[bpm=none] startProcess ignored for key {}", processDefinitionKey);
		return ProcessInstance.builder().businessKey(businessKey.orElse(null)).build();
	}

	@Override
	public void deleteProcessInstance(final String processInstanceId, final BpmEngine bpmEngine) {
		log.debug("[bpm=none] deleteProcessInstance ignored: {}", processInstanceId);
	}

	@Override
	public ActivityInstance[] findActivityInstances(final String processInstanceId, final BpmEngine bpmEngine)
			throws ProcessInstanceNotFoundException {
		return new ActivityInstance[0];
	}

	@Override
	public void createTask(final Task task, final BpmEngine bpmEngine) {
		log.debug("[bpm=none] createTask ignored");
	}

	@Override
	public Task getTask(final String taskId, final BpmEngine bpmEngine) {
		return null;
	}

	@Override
	public Task[] findTasks(final Optional<String> processInstanceBusinessKey, final BpmEngine bpmEngine) {
		return new Task[0];
	}

	@Override
	public void claimTask(final String taskId, final String taskAssignee, final BpmEngine bpmEngine) {
		log.debug("[bpm=none] claimTask ignored: {}", taskId);
	}

	@Override
	public void unclaimTask(final String taskId, final BpmEngine bpmEngine) {
		log.debug("[bpm=none] unclaimTask ignored: {}", taskId);
	}

	@Override
	public void complete(final String taskId, final List<ProcessVariable> variables, final BpmEngine bpmEngine) {
		log.debug("[bpm=none] complete ignored: {}", taskId);
	}

	@Override
	public ProcessVariable[] findVariables(final String processInstanceId, final BpmEngine bpmEngine) {
		return new ProcessVariable[0];
	}

	@Override
	public void sendMessage(final ProcessMessage processMesage, final Optional<List<ProcessVariable>> correlateKeys,
			final BpmEngine bpmEngine) {
		log.debug("[bpm=none] sendMessage ignored");
	}

}
