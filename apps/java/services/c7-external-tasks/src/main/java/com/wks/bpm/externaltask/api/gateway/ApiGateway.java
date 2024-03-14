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
package com.wks.bpm.externaltask.api.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

/**
 * @author victor.franca
 *
 */
public abstract class ApiGateway {

	@Autowired
	protected RestTemplate restTemplate;

	@Value("${wks-case-api.base.url}")
	protected String baseUrl;

	@Value("${wks-case-api.case-instance.url}")
	protected String caseInstanceUrl;

	@Value("${wks-case-api.record.url}")
	protected String recordUrl;

	@Value("${wks-case-api.case-definition.url}")
	protected String caseDefinitionUrl;

	@Value("${wks-case-api.process-definition.url}")
	protected String processDefinitionUrl;

}
