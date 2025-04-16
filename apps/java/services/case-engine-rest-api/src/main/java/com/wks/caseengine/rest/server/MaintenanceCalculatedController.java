package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.wks.caseengine.dto.MaintenanceDetailsDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.MaintenanceCalculatedService;

@RestController
@RequestMapping("api/maintenance-calculated")
public class MaintenanceCalculatedController {
	
	@Autowired
	private MaintenanceCalculatedService maintenanceCalculatedDataService;
	
	@GetMapping
	public ResponseEntity<AOPMessageVM> getMaintenanceCalculated(@RequestParam String plantId, @RequestParam String year){
		AOPMessageVM response = maintenanceCalculatedDataService.getMaintenanceCalculated(plantId,year);		
		return ResponseEntity.status(response.getCode()).body(response);
	}
}
