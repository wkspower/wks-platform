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
package com.wks.bpm.engine.camunda.operate.http.request;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.wks.bpm.engine.BpmEngine;
import com.wks.rest.client.WksHttpRequest;

/**
 * @author victor.franca
 *
 */
@Component
public class C8OperateHttpRequestFactory {

//	@Autowired
//	private JSONHttpHeadersFactory httpHeadersFactory;

//	@Value("${camunda8.rest.processdefinition.url}")
//	private String processDefinitionUrl;
//
//	@Value("${camunda8.rest.processinstance.url}")
//	private String processInstanceUrl;

	public WksHttpRequest getProcessDefinitionListRequest(BpmEngine bpmEngine) {
		// TODO Auto-generated method stub
		return null;
	}

	public WksHttpRequest getProcessInstanceListRequest(Optional<String> businessKey, BpmEngine bpmEngine) {
		// TODO Auto-generated method stub
		return null;
	}

	public WksHttpRequest getProcessDefinitionXmlRequest(String processDefinitionId, BpmEngine bpmEngine) {
		// TODO Auto-generated method stub
		return null;
	}

	public WksHttpRequest getActivityInstancesGetRequest(String processInstanceId, BpmEngine bpmEngine) {
		// TODO Auto-generated method stub
		return null;
	}

	public WksHttpRequest getTaskListRequest(String processInstanceBusinessKey, BpmEngine bpmEngine) {
		// TODO Auto-generated method stub
		return null;
	}

}
