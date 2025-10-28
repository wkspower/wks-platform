package com.wks.caseengine.rest.server;

import java.util.List;
import java.util.Map;

import com.wks.caseengine.service.AOPMaintenanceDesignBasisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.AOPMaintenanceDesignRemarksDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

@RestController
@RequestMapping("task")
public class AOPMaintenanceDesignBasisController {
	
	
	@Autowired
	private AOPMaintenanceDesignBasisService aopMaintenanceDesignBasisService;
	
	@GetMapping(value="/maintenance-design-basis")
	public AOPMessageVM getMaintenanceDesignBasis(@RequestParam String plantId,@RequestParam String year){
		 return  aopMaintenanceDesignBasisService.getMaintenanceDesignBasis(plantId,year);
	}
	
	@PostMapping(value="/maintenance-design-basis")
	public AOPMessageVM updateMaintenanceDesignBasis(@RequestParam String plantId,@RequestParam String year,@RequestParam String summary){
		 return  aopMaintenanceDesignBasisService.updateMaintenanceDesignBasis(plantId,year,summary);
	}
	
	  
}
