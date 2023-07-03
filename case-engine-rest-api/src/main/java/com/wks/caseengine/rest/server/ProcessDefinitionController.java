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
package com.wks.caseengine.rest.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.bpm.engine.client.BpmEngineClientFacade;
import com.wks.bpm.engine.model.spi.ProcessDefinition;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("process-definition")
@Tag(name = "Process Definition", description = "Access information about processes definitions in Camunda")
public class ProcessDefinitionController {

	@Autowired
	private BpmEngineClientFacade processEngineClientFacade;

	@GetMapping(value = "/{processDefinitionId}/xml", produces = MediaType.APPLICATION_XML_VALUE)
	public String get(@PathVariable final String processDefinitionId) throws Exception {
		return processEngineClientFacade.getProcessDefinitionXMLById(processDefinitionId);
	}

	@GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
	public ProcessDefinition[] find() throws Exception {
		return processEngineClientFacade.findProcessDefinitions();
	}

	@GetMapping(value = "/{bpmEngineId}/{processDefinitionId}/xml", produces = MediaType.APPLICATION_XML_VALUE)
	public String get(@PathVariable final String bpmEngineId, @PathVariable final String processDefinitionId)
			throws Exception {
		return processEngineClientFacade.getProcessDefinitionXMLById(processDefinitionId);
	}

}
