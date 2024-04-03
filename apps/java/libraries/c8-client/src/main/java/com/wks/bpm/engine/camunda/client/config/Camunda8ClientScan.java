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
@ComponentScan(basePackages = { "com.wks.bpm.engine.camunda.client", "io.camunda.zeebe" })
@ConditionalOnProperty(value = "wks.bpm.engine.camunda.version", havingValue = "camunda8", matchIfMissing = false)
public class Camunda8ClientScan {

}
