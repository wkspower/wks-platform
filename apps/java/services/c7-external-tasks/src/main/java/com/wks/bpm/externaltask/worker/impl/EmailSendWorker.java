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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.wks.bpm.externaltask.kafka.KafkaProducer;
import com.wks.bpm.externaltask.worker.WksExternalTaskHandler;

@Configuration
@ExternalTaskSubscription(topicName = "emailSend", includeExtensionProperties = true)
public class EmailSendWorker extends WksExternalTaskHandler {

    @Autowired(required = false)
    @Qualifier("kafkaProducer")
    private Optional<KafkaProducer> kafkaProducerOptional;
    
	@Value("${wks.kafka.topic.case-email-outbound}")
	protected String topic;
	
	@Autowired
	private GsonBuilder gsonBuilder;
	
	@Override
	public Optional<Map<String, Object>> doExecute(final ExternalTask externalTask, final ExternalTaskService externalTaskService) {

		String caseEmailJson = externalTask.getVariable("caseEmail");
		String caseEmailId = externalTask.getVariable("caseEmailId");
		
		JsonObject jsonObject = gsonBuilder.create().fromJson(caseEmailJson, JsonObject.class);
		jsonObject.addProperty("caseEmailId", caseEmailId);

		kafkaProducerOptional.ifPresent(kafkaProducer -> kafkaProducer.sendMessage(topic, jsonObject.toString()));
		return Optional.empty();
	}

}