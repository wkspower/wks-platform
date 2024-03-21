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
package com.wks.bpm.engine.camunda8.client;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.bpm.engine.BpmEngine;
import com.wks.bpm.engine.model.spi.ProcessInstance;
import com.wks.bpm.engine.model.spi.ProcessVariable;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;

@Component
public class C8ZeebeClient {

	@Autowired
	private ZeebeClient zeebeClient;

	@Autowired
	private C8VariablesMapper c8VariablesMapper;

	/**
	 * @param processDefinitionKey
	 * @param bpmEngine
	 * @return
	 */
	public ProcessInstance startProcess(final String processDefinitionKey, final BpmEngine bpmEngine) {

		final ProcessInstanceEvent processInstanceEvent = zeebeClient.newCreateInstanceCommand()
				.bpmnProcessId(processDefinitionKey).latestVersion().send().join();
		ProcessInstance processInstance = ProcessInstance.builder()
				.businessKey(String.valueOf(processInstanceEvent.getProcessInstanceKey())).build();

		return processInstance;
	}

	/**
	 * @param processDefinitionKey
	 * @param businessKey
	 * @param caseInstance
	 * @param bpmEngine
	 * @return the ProcessInstance
	 */
	public ProcessInstance startProcess(final String processDefinitionKey, final Optional<String> businessKey,
			final Optional<ProcessVariable> processVariable, final BpmEngine bpmEngine) {

		List<ProcessVariable> variables = processVariable.isPresent() ? Arrays.asList(processVariable.get())
				: Arrays.asList();

		return startProcess(processDefinitionKey, businessKey, variables, bpmEngine);
	}

	/**
	 * @param processDefinitionKey
	 * @param businessKey
	 * @param caseAttributes
	 * @param bpmEngine
	 * @return the ProcessInstance
	 */
	public ProcessInstance startProcess(final String processDefinitionKey, final Optional<String> businessKey,
			final List<ProcessVariable> processVariables, final BpmEngine bpmEngine) {

		Map<String, String> processVariablesMap = c8VariablesMapper.toEngineFormat(processVariables);

		if (businessKey.isPresent()) {
			processVariablesMap.put("businessKey", businessKey.get());
		}

		final ProcessInstanceEvent processInstanceEvent = zeebeClient.newCreateInstanceCommand()
				.bpmnProcessId(processDefinitionKey).latestVersion().variables(processVariablesMap).send()
				.join();

		ProcessInstance processInstance = ProcessInstance.builder()
				.businessKey(String.valueOf(processInstanceEvent.getProcessInstanceKey())).build();

		return processInstance;
	}

	/**
	 * @param processInstanceId
	 * @param variables
	 */
	public void setVariables(final String processInstanceId, final List<ProcessVariable> variables) {

		Map<String, Object> variablesMap = variables.stream()
				.collect(Collectors.toMap(ProcessVariable::getName, processVariable -> processVariable.getValue()));

		zeebeClient.newSetVariablesCommand(Long.valueOf(processInstanceId)).variables(variablesMap).send().join();
	}

}
