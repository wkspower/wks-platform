package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.wks.caseengine.dto.MaintenanceDetailsDTO;
import com.wks.caseengine.service.MaintenanceCalculatedService;

@RestController
@RequestMapping("/maintenance-calculated-data")
public class MaintenanceCalculatedController {
	
	@Autowired
	private MaintenanceCalculatedService maintenanceCalculatedDataService;
	
	@GetMapping
	public List<MaintenanceDetailsDTO> getMaintenanceCalculated(@RequestParam String plantId, @RequestParam String year){
		return maintenanceCalculatedDataService.getMaintenanceCalculated(plantId,year);		
	}
}
