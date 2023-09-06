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
import org.springframework.web.bind.annotation.RequestParam;
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

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public String findVariables(@RequestParam(name = "processInstanceId") final String processInstanceId) throws Exception {
		return variableService.findVariables(processInstanceId);
	}

}
