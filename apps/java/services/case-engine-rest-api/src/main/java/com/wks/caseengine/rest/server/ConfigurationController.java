package com.wks.caseengine.rest.server;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.ConfigurationDataDTO;
import com.wks.caseengine.service.ConfigurationService;

@RestController
@RequestMapping("task")
public class ConfigurationController {
	
	@Autowired
	private ConfigurationService configurationService;
	
	@GetMapping(value="/getConfigurationData")
	public List<Map<String, Object>> getConfigurationData(@RequestParam String year,@RequestParam UUID plantFKId) {
		return configurationService.getConfigurationData(year,plantFKId);
	}

}
