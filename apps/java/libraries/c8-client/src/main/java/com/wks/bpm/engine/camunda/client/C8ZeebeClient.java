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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wks.bpm.engine.BpmEngine;
import com.wks.bpm.engine.model.spi.ProcessInstance;

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
	public ProcessInstance startProcess(String processDefinitionKey, BpmEngine bpmEngine) {

		final ProcessInstanceEvent processInstanceEvent = zeebeClient.newCreateInstanceCommand()
				.bpmnProcessId(processDefinitionKey).latestVersion().send().join();
		ProcessInstance processInstance = ProcessInstance.builder()
				.businessKey(String.valueOf(processInstanceEvent.getProcessInstanceKey())).build();

		return processInstance;
	}

	/**
	 * @param processDefinitionKey
	 * @param businessKey
	 * @param bpmEngine
	 * @return
	 */
	public ProcessInstance startProcess(String processDefinitionKey, String businessKey, BpmEngine bpmEngine) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param processDefinitionKey
	 * @param businessKey
	 * @param caseInstance
	 * @param bpmEngine
	 * @return
	 */
	public ProcessInstance startProcess(String processDefinitionKey, String businessKey, JsonObject caseInstance,
			BpmEngine bpmEngine) {
		
		JsonArray variables = new JsonArray();
		variables.add(caseInstance);
		
		return startProcess(processDefinitionKey, businessKey, variables, bpmEngine);
	}

	/**
	 * @param processDefinitionKey
	 * @param businessKey
	 * @param caseAttributes
	 * @param bpmEngine
	 * @return
	 */
	public ProcessInstance startProcess(String processDefinitionKey, String businessKey, JsonArray caseAttributes,
			BpmEngine bpmEngine) {
		
		JsonObject processVariables = c8VariablesMapper.toJsonObject(caseAttributes);
		
		processVariables.addProperty("businessKey", businessKey);
		
		final ProcessInstanceEvent processInstanceEvent = zeebeClient.newCreateInstanceCommand()
				.bpmnProcessId(processDefinitionKey)
				.latestVersion()
				.variables(processVariables.toString())
				.send().join();
		
		ProcessInstance processInstance = ProcessInstance.builder()
				.businessKey(String.valueOf(processInstanceEvent.getProcessInstanceKey())).build();

		return processInstance;
	}

}
