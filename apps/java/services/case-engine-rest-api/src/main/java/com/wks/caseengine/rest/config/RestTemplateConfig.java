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
package com.wks.caseengine.rest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @author victor.franca
 */
@Configuration
public class RestTemplateConfig {

	// Timeout in milliseconds (10 minutes for long-running budget calculations)
	private static final int CONNECTION_TIMEOUT = 30_000;      // 30 seconds to establish connection
	private static final int READ_TIMEOUT = 600_000;           // 10 minutes to read response

	@Bean
	public RestTemplate getRestTemplate() {
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
		factory.setConnectTimeout(CONNECTION_TIMEOUT);
		factory.setConnectionRequestTimeout(CONNECTION_TIMEOUT);
		factory.setReadTimeout(READ_TIMEOUT);
		
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(factory);
		return restTemplate;
	}

}

