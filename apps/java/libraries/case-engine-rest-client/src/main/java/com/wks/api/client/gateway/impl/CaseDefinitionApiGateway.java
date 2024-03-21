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

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.wks.api.client.gateway.ApiGateway;

/**
 * @author victor.franca
 *
 */
@Component
public class CaseDefinitionApiGateway extends ApiGateway {

	public String get(final String caseDefinitionId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		return restTemplate.getForEntity(baseUrl + caseDefinitionUrl + "/" + caseDefinitionId, String.class).getBody();

	}

}
