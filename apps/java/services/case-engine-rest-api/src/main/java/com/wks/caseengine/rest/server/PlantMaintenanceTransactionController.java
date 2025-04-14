package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.wks.caseengine.entity.PlantMaintenanceTransaction;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.PlantMaintenanceTransactionService;

@RestController(value = "api/plant-maintenance")
public class PlantMaintenanceTransactionController {
	
	@Autowired
	private PlantMaintenanceTransactionService plantMaintenanceTransactionService;
	
	@GetMapping
	public ResponseEntity<AOPMessageVM> getAll() {
		AOPMessageVM response = plantMaintenanceTransactionService.getAll(); 
		return ResponseEntity.status(response.getCode()).body(response);
	}
}
