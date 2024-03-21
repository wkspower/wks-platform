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

import java.util.Optional;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.wks.api.client.gateway.impl.CaseInstanceApiGateway;
import com.wks.bpm.externaltask.kafka.KafkaProducer;
import com.wks.bpm.externaltask.worker.WksExternalTaskHandler;

@Configuration
@ExternalTaskSubscription(topicName = "caseSave", includeExtensionProperties = true)
public class CaseSaveWorker extends WksExternalTaskHandler {

	@Autowired
	private CaseInstanceApiGateway caseInstanceApiGateway;

    @Autowired(required = false)
    @Qualifier("kafkaProducer")
    private Optional<KafkaProducer> kafkaProducerOptional;
    
	@Value("${wks.kafka.topic.case-create}")
	protected String topic;

	@Override
	public void doExecute(final ExternalTask externalTask, final ExternalTaskService externalTaskService) {

		String caseInstanceJson = externalTask.getVariable("caseInstance");

		caseInstanceApiGateway.save(caseInstanceJson);

		kafkaProducerOptional.ifPresent(kafkaProducer -> kafkaProducer.sendMessage(topic, caseInstanceJson));
	}

}