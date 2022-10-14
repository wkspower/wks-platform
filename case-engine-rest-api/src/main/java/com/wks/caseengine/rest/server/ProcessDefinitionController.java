package com.wks.caseengine.rest.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.bpm.engine.client.ProcessEngineClient;

@RestController
@RequestMapping("process-definition")
public class ProcessDefinitionController {

	@Autowired
	private ProcessEngineClient processEngineClient;

	@GetMapping(value = "/{processInstanceId}/xml", produces = MediaType.APPLICATION_XML_VALUE)
	public String get(@PathVariable final String processInstanceId) throws Exception {
		return processEngineClient.getProcessDefinitionXML(processInstanceId);
	}

}
