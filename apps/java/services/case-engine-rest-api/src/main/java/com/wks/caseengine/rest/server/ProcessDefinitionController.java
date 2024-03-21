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

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.api.dto.ProcessDefinitionStartDto;
import com.wks.bpm.engine.client.facade.BpmEngineClientFacade;
import com.wks.bpm.engine.exception.ProcessDefinitionNotFoundException;
import com.wks.bpm.engine.model.spi.ProcessDefinition;
import com.wks.bpm.engine.model.spi.ProcessInstance;
import com.wks.bpm.engine.model.spi.ProcessVariable;
import com.wks.caseengine.rest.exception.RestResourceNotFoundException;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("process-definition")
@Tag(name = "Process Definition", description = "Access information about processes definitions in Camunda")
public class ProcessDefinitionController {

	@Autowired
	private BpmEngineClientFacade processEngineClientFacade;

	@PostMapping(value = "/key/{key}/start")
	public ResponseEntity<ProcessInstance> start(@PathVariable final String key,
			@RequestBody final ProcessDefinitionStartDto processDefinitionStartDto) {

		String businessKey = processDefinitionStartDto.getBusinessKey();
		Optional<List<ProcessVariable>> processVariables = Optional
				.ofNullable(processDefinitionStartDto.getProcessVariables());
		if (processVariables.isEmpty()) {
			return ResponseEntity
					.ok(processEngineClientFacade.startProcess(key, Optional.ofNullable(businessKey), Optional.empty()));
		} else {
			return ResponseEntity
					.ok(processEngineClientFacade.startProcess(key, Optional.ofNullable(businessKey), processVariables.get()));
		}
	}

	@GetMapping(value = "/{processDefinitionId}/xml", produces = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String> get(@PathVariable final String processDefinitionId) {
		try {
			return ResponseEntity.ok(processEngineClientFacade.getProcessDefinitionXMLById(processDefinitionId));
		} catch (ProcessDefinitionNotFoundException e) {
			throw new RestResourceNotFoundException(e.getMessage());
		}
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ProcessDefinition[]> find() {
		return ResponseEntity.ok(processEngineClientFacade.findProcessDefinitions());
	}

}
