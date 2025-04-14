package com.wks.caseengine.rest.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.SlowDownPlanDTO;
import com.wks.caseengine.dto.ShutDownPlanDTO;
import com.wks.caseengine.entity.PlantMaintenanceTransaction;
import com.wks.caseengine.service.ShutDownPlanService;
import com.wks.caseengine.service.SlowdownPlanService;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("api/slowdown-plan")
public class SlowdownPlanController {
	
	@Autowired
	private SlowdownPlanService slowdownPlanService;
	
	@Autowired
	private ShutDownPlanService shutDownPlanService;
	
	@GetMapping
	public ResponseEntity<List<ShutDownPlanDTO>> getPlans(@RequestParam UUID plantId, @RequestParam String type,
			@RequestParam String year) {
		List<ShutDownPlanDTO> listOfSite = null;
		try {
			listOfSite = slowdownPlanService.getPlans(plantId, type, year);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.ok(listOfSite);
	}

	@PostMapping(value = "/{plantId}")
	public ResponseEntity<List<ShutDownPlanDTO>> savePlans(@PathVariable UUID plantId,
			@RequestBody List<ShutDownPlanDTO> shutDownPlanDTOList) {
		slowdownPlanService.savePlans(plantId, shutDownPlanDTOList);
		return ResponseEntity.ok(shutDownPlanDTOList);
	}

	@PutMapping(value = "/{transactionId}")
	public ResponseEntity<List<ShutDownPlanDTO>> updatePlans(@PathVariable UUID transactionId,
			@RequestBody List<ShutDownPlanDTO> shutDownPlanDTOList) {
		slowdownPlanService.updatePlans(transactionId, shutDownPlanDTOList);
		return ResponseEntity.ok(shutDownPlanDTOList);
	}

	@DeleteMapping("/{transactionId}")
	public ResponseEntity<String> deletePlan(@PathVariable UUID transactionId) {
		shutDownPlanService.deletePlanData(transactionId);
		return ResponseEntity.ok("Plant with ID " + transactionId + " deleted successfully");
	}

}
