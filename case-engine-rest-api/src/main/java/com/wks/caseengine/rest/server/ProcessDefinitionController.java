package com.wks.caseengine.rest.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.wks.bpm.engine.camunda.client.ProcessEngineClient;

@RestController
public class ProcessDefinitionController {

	@Autowired
	private ProcessEngineClient processEngineClient;

	@GetMapping(value = "/process-definition/{processInstanceId}/xml", produces = MediaType.APPLICATION_XML_VALUE)
	public String get(@PathVariable final String processInstanceId) throws Exception {
		return processEngineClient.getProcessDefinitionXML(processInstanceId);
	}

}
