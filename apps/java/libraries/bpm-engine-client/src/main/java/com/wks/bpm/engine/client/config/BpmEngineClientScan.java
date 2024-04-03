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
package com.wks.bpm.engine.client.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author victor.franca
 *
 */
@Configuration
@ComponentScan(basePackageClasses = { com.wks.bpm.engine.client.facade.BpmEngineClientFacade.class,
		com.wks.bpm.engine.camunda.client.config.Camunda7ClientScan.class,
		com.wks.bpm.engine.camunda.client.config.Camunda8ClientScan.class })
public class BpmEngineClientScan {

}
