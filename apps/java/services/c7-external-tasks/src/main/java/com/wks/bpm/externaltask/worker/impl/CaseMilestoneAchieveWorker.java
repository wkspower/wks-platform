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

import lombok.extern.slf4j.Slf4j;

/**
 * Marks a case milestone as achieved when a process reaches the point that
 * represents it.
 *
 * <p>Sibling of {@code CaseStageUpdateWorker}: same mechanism, but milestones are
 * recorded without moving the case, so a stage can raise several as its work
 * completes. The engine treats a repeated signal for an already-achieved
 * milestone as a no-op, which is what makes this safe under external-task
 * retries.
 *
 * @author victor.franca
 */
@Slf4j
@Configuration
@ExternalTaskSubscription(topicName = "caseMilestoneAchieve", includeExtensionProperties = true)
public class CaseMilestoneAchieveWorker extends WksExternalTaskHandler {

	@Autowired
	private CaseInstanceApiGateway caseInstanceApiGateway;

	@Override
	public Optional<Map<String, Object>> doExecute(final ExternalTask externalTask,
			final ExternalTaskService externalTaskService) {

		Object milestone = externalTask.getVariable("milestone");

		// Without this guard the patch would carry the string "null" and the case
		// would record a milestone by that id. A misconfigured task should do
		// nothing rather than write nonsense onto the case.
		if (milestone == null || milestone.toString().isBlank()) {
			log.warn("External task {} on topic caseMilestoneAchieve has no 'milestone' variable — skipping",
					externalTask.getId());
			return Optional.empty();
		}

		String milestonePatch = "{\"achievedMilestone\": \"" + milestone + "\"}";

		caseInstanceApiGateway.patch(externalTask.getBusinessKey(), milestonePatch);
		return Optional.empty();
	}

}
