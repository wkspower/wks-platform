package com.wks.caseengine.rest.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.bpm.engine.client.ProcessEngineClient;
import com.wks.caseengine.bpm.BpmEngineService;

@RestController
@RequestMapping("process-definition")
public class ProcessDefinitionController {

	@Autowired
	private ProcessEngineClient processEngineClient;

	@Autowired
	private BpmEngineService bpmEngineService;

	@GetMapping(value = "/{bpmEngineId}/{processDefinitionId}/xml", produces = MediaType.APPLICATION_XML_VALUE)
	public String get(@PathVariable String bpmEngineId, @PathVariable final String processDefinitionId) throws Exception {
		return processEngineClient.getProcessDefinitionXML(processDefinitionId, bpmEngineService.get(bpmEngineId));
	}

}
