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
package com.wks.bpm.externaltask.worker;

import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.GsonBuilder;
import com.wks.api.client.gateway.impl.CaseInstanceApiGateway;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CaseStageUpdateWorker {

	@Autowired
	private CaseInstanceApiGateway caseInstanceApiGateway;

	@Autowired
	private GsonBuilder gsonBuilder;

	@JobWorker(type = "caseStageUpdate", fetchVariables = { "stage" })
	public void handleJob(final JobClient client, final ActivatedJob job) {

		log.info("Starting Worker '{}'", job.getType());

		String businessKey = gsonBuilder.create()
				.toJson((LinkedHashMap<?, ?>) job.getVariablesAsMap().get("businessKey"));

		String stage = gsonBuilder.create().toJson((LinkedHashMap<?, ?>) job.getVariablesAsMap().get("stage"));

		String stagePatch = "{\"stage\": " + "\"" + stage + "\"" + "}";
		caseInstanceApiGateway.patch(businessKey, stagePatch);
	}

}