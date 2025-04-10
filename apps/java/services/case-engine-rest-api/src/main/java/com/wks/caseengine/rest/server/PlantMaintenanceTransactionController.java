package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.wks.caseengine.entity.PlantMaintenanceTransaction;
import com.wks.caseengine.service.PlantMaintenanceTransactionService;

@RestController
public class PlantMaintenanceTransactionController {
	
	@Autowired
	private PlantMaintenanceTransactionService plantMaintenanceTransactionService;
	
	@GetMapping(value = "/plant-maintenance-records")
	public ResponseEntity<List<PlantMaintenanceTransaction>> getAll() {
		List<PlantMaintenanceTransaction> listOfSites = plantMaintenanceTransactionService.getAll(); 
	    return ResponseEntity.ok(listOfSites);
	}

}
