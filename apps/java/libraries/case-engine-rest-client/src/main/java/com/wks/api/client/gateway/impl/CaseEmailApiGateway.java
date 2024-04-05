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

/**
 * @author victor.franca
 *
 */
@Component
public class CaseEmailApiGateway extends ApiGateway {

	public void save(final String caseEmail) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> entity = new HttpEntity<>(caseEmail, headers);
		restTemplate.postForEntity(baseUrl + caseEmailUrl + "/save", entity, String.class);
	}

}
