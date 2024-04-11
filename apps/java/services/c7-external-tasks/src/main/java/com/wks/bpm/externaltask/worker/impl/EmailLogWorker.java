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

import com.wks.api.client.gateway.impl.CaseEmailApiGateway;
import com.wks.api.dto.CaseEmailDto;
import com.wks.bpm.externaltask.worker.WksExternalTaskHandler;

@Configuration
@ExternalTaskSubscription(topicName = "emailLog", includeExtensionProperties = true)
public class EmailLogWorker extends WksExternalTaskHandler {

	@Autowired
	private CaseEmailApiGateway caseEmailApiGateway;

	@Override
	public Optional<Map<String, Object>> doExecute(final ExternalTask externalTask,
			final ExternalTaskService externalTaskService) {

		String caseEmailJson = externalTask.getVariable("caseEmail");

		CaseEmailDto storedCaseEmail = caseEmailApiGateway.save(caseEmailJson);

		return Optional.of(Collections.singletonMap("caseEmailId", storedCaseEmail.get_id()));
	}

}