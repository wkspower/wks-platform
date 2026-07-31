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
package com.wks.bpm.externaltask.worker.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wks.api.client.gateway.impl.CaseDefinitionApiGateway;
import com.wks.api.client.gateway.impl.ProcessDefinitionApiGateway;
import com.wks.api.dto.ProcessDefinitionStartDto;
import com.wks.bpm.engine.model.spi.ProcessVariable;
import com.wks.bpm.externaltask.worker.WksExternalTaskHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@ExternalTaskSubscription(topicName = "businessProcessStart", includeExtensionProperties = true)
public class BusinessProcessStartWorker extends WksExternalTaskHandler {

	@Autowired
	private CaseDefinitionApiGateway caseDefinitionApiGateway;

	@Autowired
	private ProcessDefinitionApiGateway processDefinitionApiGateway;

	@Autowired
	private GsonBuilder gsonBuilder;

	@Override
	public Optional<Map<String, Object>> doExecute(final ExternalTask externalTask, final ExternalTaskService externalTaskService) {

		String caseInstanceJson = externalTask.getVariable("caseInstance");

		JsonObject jsonObject = gsonBuilder.create().fromJson(caseInstanceJson, JsonObject.class);

		String processDefinitionKey = getProcessDefinitionId(jsonObject);

		// A case type need not have a lifecycle process. Cases imported from a model
		// drive themselves through per-stage processes, and engine-less deployments
		// have none at all — the config schema documents the field as optional. Raising
		// an incident for a legitimate configuration would put a permanent failure
		// against every such case, so this skips instead.
		if (processDefinitionKey == null || processDefinitionKey.isBlank()) {
			log.debug("Case definition {} declares no stagesLifecycleProcessKey — nothing to start for case {}",
					jsonObject.get("caseDefinitionId"), jsonObject.get("businessKey"));
			return Optional.empty();
		}

		JsonArray caseAttributesArray = jsonObject.get("attributes").getAsJsonArray();
		List<ProcessVariable> processVariables = new ArrayList<>();
		for (JsonElement element : caseAttributesArray) {
			processVariables.add(gsonBuilder.create().fromJson(element, ProcessVariable.class));
		}

		processDefinitionApiGateway.start(processDefinitionKey, ProcessDefinitionStartDto.builder()
				.processVariables(processVariables).businessKey(jsonObject.get("businessKey").getAsString()).build());
		return Optional.empty();
	}

	/**
	 * @return the case type's lifecycle process key, or null when it declares none
	 */
	private String getProcessDefinitionId(final JsonObject caseInstanceJson) {
		String caseDefJsonString = caseDefinitionApiGateway.get(caseInstanceJson.get("caseDefinitionId").getAsString());
		JsonObject caseDefJson = gsonBuilder.create().fromJson(caseDefJsonString, JsonObject.class);

		JsonElement processDefKey = caseDefJson.get("stagesLifecycleProcessKey");
		if (processDefKey == null || processDefKey.isJsonNull()) {
			return null;
		}

		return processDefKey.getAsString();
	}

}