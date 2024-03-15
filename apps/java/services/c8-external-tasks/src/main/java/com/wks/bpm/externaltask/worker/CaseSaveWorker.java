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
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.GsonBuilder;
import com.wks.bpm.externaltask.api.gateway.impl.CaseInstanceApiGateway;
import com.wks.bpm.externaltask.kafka.KafkaProducer;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CaseSaveWorker {

	@Autowired
	private CaseInstanceApiGateway caseInstanceApiGateway;
	
	@Autowired
	private GsonBuilder gsonBuilder;

	@Autowired(required = false)
	@Qualifier("kafkaProducer")
	private Optional<KafkaProducer> kafkaProducerOptional;

	@Value("${wks.kafka.topic.case-create}")
	protected String topic;

	@JobWorker(type = "caseSave", fetchVariables = { "caseInstance" })
	public void handleJobFoo(final JobClient client, final ActivatedJob job) {
		
		log.info("Starting Worker '{}'", job.getType());
		
		String caseInstanceJson = gsonBuilder.create()
				.toJson((LinkedHashMap<?, ?>) job.getVariablesAsMap().get("caseInstance"));

		caseInstanceApiGateway.save(caseInstanceJson);

		kafkaProducerOptional.ifPresent(kafkaProducer -> kafkaProducer.sendMessage(topic, caseInstanceJson));
	}

}