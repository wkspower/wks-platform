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
import org.springframework.stereotype.Component;

import com.wks.bpm.engine.BpmEngine;
import com.wks.bpm.engine.model.impl.ProcessDefinitionImpl;
import com.wks.bpm.engine.model.spi.ProcessDefinition;
import com.wks.bpm.engine.model.spi.ProcessInstance;
import com.wks.bpm.engine.model.spi.ProcessVariable;
import com.wks.bpm.engine.model.spi.ProcessVariableType;

import io.camunda.operate.CamundaOperateClient;
import io.camunda.operate.exception.OperateException;
import io.camunda.operate.model.ProcessInstanceState;
import io.camunda.operate.model.Variable;
import io.camunda.operate.search.ProcessDefinitionFilter;
import io.camunda.operate.search.SearchQuery;
import io.camunda.operate.search.VariableFilter;
import io.camunda.operate.search.VariableFilterBuilder;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class C8OperateClient {

	@Autowired
	private CamundaOperateClient operateClient;

	public String getProcessDefinitionXML(String processDefinitionId, final BpmEngine bpmEngine) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @param processDefinitionKey
	 * @param businessKey
	 * @param activityIdIn
	 * @param bpmEngine
	 * @return
	 */
	public ProcessInstance[] searchProcessInstances(Optional<String> processDefinitionKey, Optional<String> businessKey,
			Optional<String> activityIdIn, BpmEngine bpmEngine) {

		try {

			VariableFilterBuilder filterBuilder = VariableFilter.builder()
					.name(businessKey.isPresent() ? "businessKey" : null)
					// adding double quotes because that's how operate is converting string
					// variables on variable queries.
					.value(businessKey.isPresent() ? ("\"" + businessKey.get() + "\"") : null);

			SearchQuery searchQuery = new SearchQuery.Builder().filter(filterBuilder.build()).build();

			List<Variable> variables = operateClient.searchVariables(searchQuery).stream().filter(variable -> {
				try {
					return ProcessInstanceState.ACTIVE
							.equals(operateClient.getProcessInstance(variable.getProcessInstanceKey()).getState());
				} catch (OperateException e) {
					log.error("Error searching process instances in zeebe", e);
					e.printStackTrace();
					return false;
				}
			}).toList();

			return variables.stream()
					.map(variable -> ProcessInstance.builder().businessKey(variable.getValue())
							.id(String.valueOf(variable.getProcessInstanceKey())).tenantId(variable.getTenantId())
							.build())
					.toArray(ProcessInstance[]::new);

		} catch (OperateException e) {
			log.error("Error searching process instances in zeebe", e);
			e.printStackTrace();
			return new ProcessInstance[0];
		}
	}

	/**
	 * @param processDefinitionId
	 * @param bpmEngine
	 * @return the process definition xml
	 */
	public String getProcessDefinitionXMLById(String processDefinitionId, BpmEngine bpmEngine) {
		try {
			return operateClient.getProcessDefinitionXml(Long.valueOf(processDefinitionId));
		} catch (NumberFormatException | OperateException e) {
			log.error("Error retrieving process definition xml in zeebe", e);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param processInstanceId
	 * @param bpmEngine
	 * @return the variable list as a json string
	 */
	public ProcessVariable[] findVariables(String processInstanceId, BpmEngine bpmEngine) {

		VariableFilterBuilder filterBuilder = VariableFilter.builder()
				.processInstanceKey(Long.valueOf(processInstanceId));

		try {
			SearchQuery searchQuery = new SearchQuery.Builder()
					.filter(filterBuilder.processInstanceKey(Long.valueOf(processInstanceId)).build()).build();

			return operateClient.searchVariables(searchQuery).stream()
					.map(o -> ProcessVariable.builder().name(o.getName()).type(ProcessVariableType.JSON.getValue())
							.value(o.getValue()).build())

					.toArray(ProcessVariable[]::new);

		} catch (NumberFormatException | OperateException e) {
			log.error("Error retrieving variables in zeebe", e);
			e.printStackTrace();
			return new ProcessVariable[0];
		}

	}

	/**
	 * @param bpmEngine
	 * @return ProcessDefinition array
	 */
	public ProcessDefinition[] findProcessDefinitions(BpmEngine bpmEngine) {
		try {
			SearchQuery searchQuery = new SearchQuery.Builder().filter(ProcessDefinitionFilter.builder().build())
					.build();
			return operateClient.searchProcessDefinitions(searchQuery).stream()

					.map(o ->

					ProcessDefinitionImpl.builder()
					.bpmEngineId(bpmEngine.getId())
					.id(o.getBpmnProcessId())
					.key(String.valueOf(o.getKey()))
					.version(String.valueOf(o.getVersion()))
					.name(o.getName())
					.build())

					.toArray(ProcessDefinitionImpl[]::new);

		} catch (OperateException e) {
			log.error("Error retrieving process definitions from zeebe", e);
			e.printStackTrace();
			return new ProcessDefinition[0];
		}
	}

}
