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
package com.wks.bpm.externaltask.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author victor.franca
 *
 */
@Component
@ConditionalOnProperty(value = "wks.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaProducer {

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	public void sendMessage(String topic, String message) {
		kafkaTemplate.send(topic, message);
	}
}