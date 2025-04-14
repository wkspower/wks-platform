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
import com.wks.caseengine.service.ConfigurationService;

@RestController
@RequestMapping("api/configuration")
public class ConfigurationController {
	
	@Autowired
	private ConfigurationService configurationService;
	
	@GetMapping
	public List<ConfigurationDTO> getConfigurationData(@RequestParam String year,@RequestParam UUID plantFKId) {
		return configurationService.getConfigurationData(year,plantFKId);
	}
	
	@PostMapping
	public List<ConfigurationDTO> saveConfigurationData(@RequestParam String year, @RequestBody List<ConfigurationDTO> configurationDTOList) {
		configurationService.saveConfigurationData(year,configurationDTOList);
		return configurationDTOList;
	}
	
	@GetMapping(value="/pe")
public  List<Map<String, Object>> getNormAttributeTransactionReceipeSp(@RequestParam String year,@RequestParam String plantId){
		return	 configurationService.getNormAttributeTransactionReceipe(year,plantId);
		
	}

	
	@PostMapping(value="/pe/update")
	public List<NormAttributeTransactionReceipe> updateCalculatedConsumptionNorms(@RequestParam String year,@RequestParam String plantId,@RequestBody List<NormAttributeTransactionReceipeRequestDTO> normAttributeTransactionReceipeDTOList){
		return configurationService.updateCalculatedConsumptionNorms(year,plantId,normAttributeTransactionReceipeDTOList);
		
	}

}
