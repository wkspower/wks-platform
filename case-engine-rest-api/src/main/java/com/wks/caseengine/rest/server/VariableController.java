package com.wks.caseengine.rest.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.variables.VariableService;

@RestController
public class VariableController {

	@Autowired
	private VariableService variableService;

	@GetMapping(value = "/variable/{processInstanceId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public String findVariables(@PathVariable String processInstanceId) {
		return variableService.findVariables(processInstanceId);
	}

}
