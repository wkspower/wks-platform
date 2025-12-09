package com.wks.caseengine.rest.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
	
}
