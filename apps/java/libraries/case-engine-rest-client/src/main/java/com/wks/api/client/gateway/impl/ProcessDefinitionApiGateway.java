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
package com.wks.api.client.gateway.impl;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.wks.api.client.gateway.ApiGateway;
import com.wks.api.dto.ProcessDefinitionStartDto;

/**
 * @author victor.franca
 *
 */
@Component
public class ProcessDefinitionApiGateway extends ApiGateway {

	public void start(final String processDefinitionKey, final ProcessDefinitionStartDto processDefinitionStartDto) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<ProcessDefinitionStartDto> entity = new HttpEntity<ProcessDefinitionStartDto>(
				processDefinitionStartDto, headers);
		restTemplate.postForEntity(baseUrl + processDefinitionUrl + "/key/" + processDefinitionKey + "/start", entity,
				String.class);
	}

}
