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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.wks.api.client.gateway.ApiGateway;
import com.wks.api.dto.CaseInstanceDto;

/**
 * @author victor.franca
 *
 */
@Component
public class CaseInstanceApiGateway extends ApiGateway {

	public CaseInstanceDto get(final String businessKey) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> entity = new HttpEntity<>(headers);
		return restTemplate
				.exchange(baseUrl + caseInstanceUrl + "/" + businessKey, HttpMethod.GET, entity, CaseInstanceDto.class)
				.getBody();
	}

	public CaseInstanceDto start(final CaseInstanceDto caseInstance) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<CaseInstanceDto> entity = new HttpEntity<>(caseInstance, headers);
		return restTemplate.postForEntity(baseUrl + caseInstanceUrl, entity, CaseInstanceDto.class).getBody();
	}

	public void save(final String caseInstance) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> entity = new HttpEntity<>(caseInstance, headers);
		restTemplate.postForEntity(baseUrl + caseInstanceUrl + "/save", entity, String.class);
	}

	public void patch(final String businessKey, final String patch) {

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/merge-patch+json");

		HttpEntity<String> entity = new HttpEntity<>(patch, headers);

		restTemplate.patchForObject(baseUrl + caseInstanceUrl + "/" + businessKey, entity, String.class);
	}

}
