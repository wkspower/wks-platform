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
package com.wks.bpm.engine.camunda.client.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author victor.franca
 *
 */
@Configuration
@ConditionalOnProperty(name = "wks.bpm.engine", havingValue = "camunda7", matchIfMissing = true)
@ComponentScan(basePackages = { "com.wks.bpm.engine.camunda.client", "org.camunda.community.rest.client.springboot" })
public class Camunda7ClientScan {

}
