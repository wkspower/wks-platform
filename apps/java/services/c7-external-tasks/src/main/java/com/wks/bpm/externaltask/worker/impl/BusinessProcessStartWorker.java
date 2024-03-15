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

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.wks.bpm.externaltask.api.gateway.impl.CaseDefinitionApiGateway;
import com.wks.bpm.externaltask.api.gateway.impl.ProcessDefinitionApiGateway;
import com.wks.bpm.externaltask.worker.WksExternalTaskHandler;

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
	public void doExecute(final ExternalTask externalTask, final ExternalTaskService externalTaskService) {

		String caseInstanceJson = externalTask.getVariable("caseInstance");

		String processDefKey = getProcessDefinitionId(
				gsonBuilder.create().fromJson(caseInstanceJson, JsonObject.class));

		processDefinitionApiGateway.start(processDefKey, caseInstanceJson);

	}

	private String getProcessDefinitionId(final JsonObject caseInstanceJson) {
		String caseDefJsonString = caseDefinitionApiGateway.get(caseInstanceJson.get("caseDefinitionId").getAsString());
		JsonObject caseDefJson = gsonBuilder.create().fromJson(caseDefJsonString, JsonObject.class);
		String processDefKey = caseDefJson.get("stagesLifecycleProcessKey").getAsString();
		return processDefKey;
	}

}