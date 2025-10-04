package com.wks.caseengine.rest.server;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.ConfigurationTypeService;


@RestController
@RequestMapping("task")
public class ConfigurationTypeController {
	
	@Autowired
	private ConfigurationTypeService configurationTypeService;
	
	@GetMapping(value="/configuration-type-data")
	public AOPMessageVM getConfigurationTypeData() {
		return configurationTypeService.getConfigurationTypeData();
	}
}