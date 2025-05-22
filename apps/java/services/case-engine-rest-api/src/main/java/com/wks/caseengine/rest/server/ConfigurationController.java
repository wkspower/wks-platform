package com.wks.caseengine.rest.server;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.ConfigurationDTO;
import com.wks.caseengine.dto.NormAttributeTransactionReceipeDTO;
import com.wks.caseengine.dto.NormAttributeTransactionReceipeRequestDTO;
import com.wks.caseengine.entity.NormAttributeTransactionReceipe;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.ConfigurationService;

@RestController
@RequestMapping("task")
public class ConfigurationController {
	
	@Autowired
	private ConfigurationService configurationService;
	
	@GetMapping(value="/getConfigurationData")
	public List<ConfigurationDTO> getConfigurationData(@RequestParam String year,@RequestParam UUID plantFKId) {
		return configurationService.getConfigurationData(year,plantFKId);
	}
	
	@GetMapping(value="/configuration/intermediate-values")
	public AOPMessageVM getConfigurationIntermediateValues(@RequestParam String year,@RequestParam UUID plantFKId) {
		return configurationService.getConfigurationIntermediateValues(year,plantFKId);
	}
	
	@GetMapping(value="/get/configuration/intermediate-values")
	public AOPMessageVM getConfigurationIntermediateValuesData(@RequestParam String year,@RequestParam String plantFKId) {
		return configurationService.getConfigurationIntermediateValuesData(year,plantFKId);
	}
	
	@PostMapping(value="/saveConfigurationData")
	public List<ConfigurationDTO> saveConfigurationData(@RequestParam String year,@RequestParam String plantFKId, @RequestBody List<ConfigurationDTO> configurationDTOList) {
		configurationService.saveConfigurationData(year,plantFKId,configurationDTOList);
		return configurationDTOList;
	}
	
	@GetMapping(value="/getPeConfigData")
	public  List<Map<String, Object>> getNormAttributeTransactionReceipeSp(@RequestParam String year,@RequestParam String plantId){
		return	 configurationService.getNormAttributeTransactionReceipe(year,plantId);
		
	}
	
	@PostMapping(value="/updatePeConfigData")
	public List<NormAttributeTransactionReceipe> updateCalculatedConsumptionNorms(@RequestParam String year,@RequestParam String plantId,@RequestBody List<NormAttributeTransactionReceipeRequestDTO> normAttributeTransactionReceipeDTOList){
		return configurationService.updateCalculatedConsumptionNorms(year,plantId,normAttributeTransactionReceipeDTOList);
		
	}

}
