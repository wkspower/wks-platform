package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.ConfigurationDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.PackagingConsumablesService;
import com.wks.caseengine.service.PriceDifferentialService;
import com.wks.caseengine.service.QualityParametersService;

@RestController
@RequestMapping("task")
public class QualityParametersController {
	
	@Autowired
	private QualityParametersService qualityParametersService;
	
	@GetMapping(value="/quality-parameters")
	public AOPMessageVM getQualityParameters(@RequestParam String plantId,@RequestParam String year){
		 return  qualityParametersService.getQualityParameters(plantId,year);
	}
	
	@PostMapping(value="/quality-parameters")
	public AOPMessageVM saveQualityParameters(@RequestParam String year,@RequestParam String plantFKId, @RequestBody List<ConfigurationDTO> configurationDTOList) {
		return 	qualityParametersService.saveQualityParameters(year,plantFKId,configurationDTOList);
	}
	
}

