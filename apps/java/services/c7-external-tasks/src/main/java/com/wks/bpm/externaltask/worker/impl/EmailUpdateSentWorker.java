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

import java.util.Map;
import java.util.Optional;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.google.gson.JsonObject;
import com.wks.api.client.gateway.impl.CaseEmailApiGateway;
import com.wks.bpm.externaltask.worker.WksExternalTaskHandler;

@Configuration
@ExternalTaskSubscription(topicName = "emailUpdateSent", includeExtensionProperties = true)
public class EmailUpdateSentWorker extends WksExternalTaskHandler {

	@Autowired
	private CaseEmailApiGateway caseEmailApiGateway;

	@Override
	public Optional<Map<String, Object>> doExecute(final ExternalTask externalTask,
			final ExternalTaskService externalTaskService) {

		String caseEmailId = externalTask.getVariable("caseEmailId");
		String sentDateTime = externalTask.getVariable("sentDateTime");

		JsonObject patch = new JsonObject();
		patch.addProperty("status", "sent");
		patch.addProperty("receivedDateTime", sentDateTime);

		caseEmailApiGateway.mergePatch(caseEmailId, patch.toString());

		return Optional.empty();
	}

}