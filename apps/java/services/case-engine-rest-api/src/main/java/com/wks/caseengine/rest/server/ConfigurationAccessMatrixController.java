package com.wks.caseengine.rest.server;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.ConfigurationAccessMatrixService;

@RestController
@RequestMapping("task")
public class ConfigurationAccessMatrixController {
	
	@Autowired
	private ConfigurationAccessMatrixService configurationAccessMatrixService;
	
	@GetMapping(value="/access/matrix")
	public ResponseEntity<AOPMessageVM> getConfigurationAccessMatrix(@RequestParam String plantId,@RequestParam String siteId,@RequestParam String verticalId){
		AOPMessageVM response	=configurationAccessMatrixService.getConfigurationAccessMatrix(plantId,siteId,verticalId);
		return ResponseEntity.status(response.getCode()).body(response);
	}
	
	
}
