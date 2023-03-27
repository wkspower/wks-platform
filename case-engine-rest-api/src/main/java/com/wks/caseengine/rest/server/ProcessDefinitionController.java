package com.wks.caseengine.rest.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.bpm.engine.BpmEngine;
import com.wks.bpm.engine.client.BpmEngineClientFacade;
import com.wks.bpm.engine.model.spi.ProcessDefinition;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("process-definition")
@Tag(name = "Process Definition", description = "Access information about processes definitions in Camunda")
public class ProcessDefinitionController {

	@Autowired
	private BpmEngineClientFacade processEngineClientFacade;

<<<<<<< Updated upstream
	@GetMapping(value = "/{processDefinitionId}/xml", produces = MediaType.APPLICATION_XML_VALUE)
	public String get(@PathVariable final String processDefinitionId) throws Exception {
		return processEngineClientFacade.getProcessDefinitionXMLById(processDefinitionId);
=======
	@Autowired
	private BpmEngineService bpmEngineService;


	@GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ProcessDefinition> find()
			throws Exception {
		
		List<ProcessDefinition> procDefs = new ArrayList<>();
		
		List<BpmEngine> bpmEngine = bpmEngineService.find();

		bpmEngine.forEach(o -> {
			try {
				procDefs.addAll(
						Arrays.asList(processEngineClientFacade.findProcessDefinitions(bpmEngineService.get(o.getId()))));
			} catch (Exception e) {
				// TODO exception handling
				e.printStackTrace();
			}
		});
		
		return procDefs;
>>>>>>> Stashed changes
	}

	@GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
	public ProcessDefinition[] find() throws Exception {
		return processEngineClientFacade.findProcessDefinitions();
	}

	@GetMapping(value = "/{bpmEngineId}/{processDefinitionId}/xml", produces = MediaType.APPLICATION_XML_VALUE)
	public String get(@PathVariable final String bpmEngineId, @PathVariable final String processDefinitionId)
			throws Exception {
		return processEngineClientFacade.getProcessDefinitionXMLById(processDefinitionId, bpmEngineService.get(bpmEngineId));
	}

}
