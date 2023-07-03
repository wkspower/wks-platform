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
package com.wks.bpm.engine.camunda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({ "com.wks.caseengine", "com.wks.bpm.engine", "com.wks.rest.client", "com.wks.api.security" })
public class ExternalServiceClientApp {

	public static void main(final String[] args) {
		SpringApplication.run(ExternalServiceClientApp.class, args);
	}

}
