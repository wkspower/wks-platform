package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.MaintenanceCalculatedDataDTO;
import com.wks.caseengine.dto.MaintenanceDetailsDTO;
import com.wks.caseengine.service.MaintenanceCalculatedDataService;

@RestController
@RequestMapping("task")
public class MaintenanceCalculatedDataController {
	
	@Autowired
	private MaintenanceCalculatedDataService maintenanceCalculatedDataService;
	
	@GetMapping(value="/getMaintenanceCalculatedData")
	public List<MaintenanceDetailsDTO> getMaintenanceCalculatedData(@RequestParam String plantId, @RequestParam String year){
		return maintenanceCalculatedDataService.getMaintenanceCalculatedData(plantId,year);		
	}
}
