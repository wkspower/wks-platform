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

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.wks.api.client.gateway.impl.CaseInstanceApiGateway;
import com.wks.api.dto.CaseInstanceDto;
import com.wks.api.dto.CaseOwnerDto;
import com.wks.bpm.externaltask.worker.WksExternalTaskHandler;

@Configuration
@ExternalTaskSubscription(topicName = "caseStart", includeExtensionProperties = true)
public class CaseStartWorker extends WksExternalTaskHandler {

	@Autowired
	private CaseInstanceApiGateway caseInstanceApiGateway;

	@Autowired
	private GsonBuilder gsonBuilder;

	@Override
	public Optional<Map<String, Object>> doExecute(final ExternalTask externalTask,
			final ExternalTaskService externalTaskService) {

		String caseEmailJson = externalTask.getVariable("caseEmail");
		String caseOwnerEmail = externalTask.getVariable("caseOwnerEmail");

		JsonObject caseEmailjsonObject = gsonBuilder.create().fromJson(caseEmailJson, JsonObject.class);

		CaseInstanceDto caseInstanceParam = CaseInstanceDto.builder()
				.caseDefinitionId(caseEmailjsonObject.get("caseDefinitionId").getAsString())
				.owner(CaseOwnerDto.builder().id(caseOwnerEmail).name(caseOwnerEmail).email(caseOwnerEmail).build())
				.build();

		CaseInstanceDto caseInstance = caseInstanceApiGateway.start(caseInstanceParam);

		return Optional.of(Collections.singletonMap("caseInstanceBusinessKey", caseInstance.getBusinessKey()));

	}

}