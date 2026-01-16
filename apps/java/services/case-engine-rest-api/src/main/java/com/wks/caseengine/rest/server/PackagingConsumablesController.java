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

@RestController
@RequestMapping("task")
public class PackagingConsumablesController {
	
	@Autowired
	private PackagingConsumablesService packagingConsumablesService;
	
	@GetMapping(value="/packaging-consumables")
	public AOPMessageVM getPackagingConsumables(@RequestParam String plantId,@RequestParam String year){
		 return  packagingConsumablesService.getPackagingConsumables(plantId,year);
	}
	
	@PostMapping(value="/packaging-consumables")
	public AOPMessageVM savePackagingConsumables(@RequestParam String year,@RequestParam String plantFKId, @RequestBody List<ConfigurationDTO> configurationDTOList) {
		return 	packagingConsumablesService.savePackagingConsumables(year,plantFKId,configurationDTOList);
	}
	
}

