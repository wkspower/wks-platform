package com.wks.caseengine.rest.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.cases.definition.CaseDefinitionService;

@RestController
@RequestMapping("healthCheck")
public class HealthCheckController {

	@Autowired
	private CaseDefinitionService caseDefinitionService;

	@GetMapping(value = "/")
	public String check() throws Exception {
		caseDefinitionService.find();
		return "success";
	}

}
