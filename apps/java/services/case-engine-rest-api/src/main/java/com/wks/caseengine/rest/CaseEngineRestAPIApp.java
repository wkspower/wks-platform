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
package com.wks.caseengine.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {
	UserDetailsServiceAutoConfiguration.class,
	MongoAutoConfiguration.class, 
	MongoDataAutoConfiguration.class
})
@ComponentScan (
	basePackageClasses = { com.wks.api.security.config.ApiSecurityScan.class, com.wks.caseengine.config.CaseEngineScan.class },
	basePackages = {"com.wks.caseengine.rest.config", "com.wks.caseengine.rest.server"}
)
public class CaseEngineRestAPIApp {

	public static void main(final String[] args) {
		SpringApplication.run(CaseEngineRestAPIApp.class, args);
	}

}
