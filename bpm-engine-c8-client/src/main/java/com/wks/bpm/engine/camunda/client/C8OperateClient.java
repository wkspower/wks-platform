package com.wks.bpm.engine.camunda.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.wks.bpm.engine.BpmEngine;
import com.wks.bpm.engine.camunda.operate.http.request.C8OperateHttpRequestFactory;

@Component
public class C8OperateClient {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private C8OperateHttpRequestFactory camundaHttpRequestFactory;

	public String getProcessDefinitionXML(String processDefinitionId, final BpmEngine bpmEngine) {
		throw new UnsupportedOperationException();
	}

}
