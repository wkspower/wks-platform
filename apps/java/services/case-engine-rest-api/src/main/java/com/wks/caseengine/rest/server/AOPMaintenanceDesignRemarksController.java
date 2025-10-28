package com.wks.caseengine.rest.server;

import java.util.List;
import com.wks.caseengine.service.AOPMaintenanceDesignRemarksService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.AOPMaintenanceDesignRemarksDTO;
import com.wks.caseengine.dto.AOPSummaryDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

@RestController
@RequestMapping("task")
public class AOPMaintenanceDesignRemarksController {
	
	
	@Autowired
	private AOPMaintenanceDesignRemarksService aopMaintenanceDesignRemarksService;
	
	@GetMapping(value="/maintenance-design-remarks")
	public AOPMessageVM getMaintenanceDesignRemarks(@RequestParam String plantId,@RequestParam String year){
		 return  aopMaintenanceDesignRemarksService.getMaintenanceDesignRemarks(plantId,year);
	}
	
	@PostMapping(value="/maintenance-design-remarks")
	public AOPMessageVM updateMaintenanceDesignRemarks(@RequestParam String plantId,@RequestParam String year,@RequestBody AOPSummaryDTO aopSummaryDTO){
		 return  aopMaintenanceDesignRemarksService.updateMaintenanceDesignRemarks(plantId,year, aopSummaryDTO.getSummary());
	}
	
	  
}
