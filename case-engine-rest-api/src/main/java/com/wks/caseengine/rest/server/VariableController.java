package com.wks.caseengine.rest.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.variables.VariableService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("variable")
@Tag(name = "Variables", description = "Access information about variables in Camunda")
public class VariableController {

	@Autowired
	private VariableService variableService;

	@GetMapping(value = "/{bpmEngineId}/{processInstanceId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public String findVariables(@PathVariable final String bpmEngineId, @PathVariable final String processInstanceId)
			throws Exception {
		return variableService.findVariables(processInstanceId, bpmEngineId);
	}

}
