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

import com.wks.api.client.gateway.impl.CaseInstanceApiGateway;
import com.wks.bpm.externaltask.worker.WksExternalTaskHandler;

@Configuration
@ExternalTaskSubscription(topicName = "caseStageUpdate", includeExtensionProperties = true)
public class CaseStageUpdateWorker extends WksExternalTaskHandler {

	@Autowired
	private CaseInstanceApiGateway caseInstanceApiGateway;

	@Override
	public Optional<Map<String, Object>> doExecute(final ExternalTask externalTask, final ExternalTaskService externalTaskService) {
		String stagePatch = "{\"stage\": " + "\"" + externalTask.getVariable("stage") + "\"" + "}";
		caseInstanceApiGateway.patch(externalTask.getBusinessKey(), stagePatch);
		return Optional.empty();
	}

}