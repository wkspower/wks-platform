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
import com.wks.api.dto.CaseEmailDto;

/**
 * @author victor.franca
 *
 */
@Component
public class CaseEmailApiGateway extends ApiGateway {

	public CaseEmailDto save(final String caseEmail) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> entity = new HttpEntity<>(caseEmail, headers);
		return restTemplate.postForEntity(baseUrl + caseEmailUrl + "/save", entity, CaseEmailDto.class).getBody();

	}

	public void mergePatch(final String caseEmailId, final String patch) {

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/merge-patch+json");

		HttpEntity<String> entity = new HttpEntity<>(patch, headers);

		restTemplate.patchForObject(baseUrl + caseEmailUrl + "/" + caseEmailId, entity, String.class);
	}

}
