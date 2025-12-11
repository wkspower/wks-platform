package com.wks.caseengine.rest.server;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.PlantRequirementDTO;
import com.wks.caseengine.service.ConsumptionService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("task")
@Tag(name = "Comsumption", description = "test Site")
public class ConsumptionController {
	
	@Autowired
	private ConsumptionService consumptionService; 
	
	@GetMapping(value = "/plant-requirement/{plantId}/{financialYear}")
	public ResponseEntity<List<PlantRequirementDTO>> getAllSites(@PathVariable UUID plantId, @PathVariable String financialYear) {
		List<PlantRequirementDTO> listOfCppConsumptions = consumptionService.getCppConsumptions(plantId, financialYear);
	    return ResponseEntity.ok(listOfCppConsumptions);
	}

}
