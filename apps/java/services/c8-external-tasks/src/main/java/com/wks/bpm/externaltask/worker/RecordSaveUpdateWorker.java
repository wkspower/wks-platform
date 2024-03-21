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
import com.wks.api.client.gateway.impl.RecordApiGateway;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RecordSaveUpdateWorker {

	@Autowired
	private RecordApiGateway recordApiGateway;
	
	@Autowired
	private GsonBuilder gsonBuilder;

	@JobWorker(type = "recordSave", fetchVariables = { "record", "recordTypeId" })
	public void handleJobFoo(final JobClient client, final ActivatedJob job) {
		
		log.info("Starting Worker '{}'", job.getType());
		
		String recordJsonString = gsonBuilder.create()
				.toJson((LinkedHashMap<?, ?>) job.getVariablesAsMap().get("record"));

		String recordTypeId = gsonBuilder.create()
				.toJson((LinkedHashMap<?, ?>) job.getVariablesAsMap().get("recordTypeId"));

		recordApiGateway.save(recordTypeId, recordJsonString);
	}

}