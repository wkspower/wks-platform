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
package com.wks.bpm.engine.camunda.client;

import org.springframework.stereotype.Component;

import com.wks.bpm.engine.BpmEngine;

@Component
public class C8OperateClient {

//	@Autowired
//	private RestTemplate restTemplate;
//
//	@Autowired
//	private C8OperateHttpRequestFactory camundaHttpRequestFactory;

	public String getProcessDefinitionXML(String processDefinitionId, final BpmEngine bpmEngine) {
		throw new UnsupportedOperationException();
	}

}
