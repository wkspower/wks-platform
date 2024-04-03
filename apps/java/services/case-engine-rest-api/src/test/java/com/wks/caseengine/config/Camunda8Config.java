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
package com.wks.caseengine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.camunda.operate.CamundaOperateClient;
import io.camunda.zeebe.client.ZeebeClient;

/**
 * @author victor.franca
 *
 */
/*
 * This is required since Spring-zeebe is injecting the CamundaOperateClient and
 * ZeebeClient beans regardless of our ComponentScan/Configuration conditions.
 * It should be removed after figuring out a way to tell spring-boot not to load
 * those beans.
 */
@Configuration
public class Camunda8Config {

	@Bean
	public CamundaOperateClient getCamundaOperateClient() {
		return CamundaOperateClient.builder().setup().build();
	}

	@Bean
	public ZeebeClient getZeebeClient() {
		return ZeebeClient.newClient();
	}

}
