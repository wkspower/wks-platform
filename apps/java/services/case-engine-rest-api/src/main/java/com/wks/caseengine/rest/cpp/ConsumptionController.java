package com.wks.caseengine.rest.cpp;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.CalculatedProcessDemandDTO;
import com.wks.caseengine.dto.PlantRequirementDTO;
import com.wks.caseengine.dto.ProcessDemandUpdateRequest;
import com.wks.caseengine.dto.ProcessDemandUpdateResponse;
import com.wks.caseengine.cpp.service.ConsumptionService;

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

	@GetMapping(value = "/plant-requirement/{financialYear}")
	public ResponseEntity<List<CalculatedProcessDemandDTO>> getProcessDemand(@PathVariable String financialYear) {
		List<CalculatedProcessDemandDTO> listOfCppConsumptions = consumptionService.getProcessDemand(financialYear);
	    return ResponseEntity.ok(listOfCppConsumptions);
	}

	@PostMapping(value = "/plant-requirement/{financialYear}")
	public ResponseEntity<ProcessDemandUpdateResponse> updateProcessDemand(
			@PathVariable String financialYear,
			@RequestBody List<ProcessDemandUpdateRequest> requests) {
		ProcessDemandUpdateResponse response = consumptionService.updateProcessDemand(financialYear, requests);
		return ResponseEntity.ok(response);
	}

}

