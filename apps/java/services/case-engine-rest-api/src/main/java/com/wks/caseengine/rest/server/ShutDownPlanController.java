package com.wks.caseengine.rest.server;

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
import org.springframework.web.bind.annotation.RequestBody;

import com.wks.caseengine.dto.MonthWiseDataDTO;
import com.wks.caseengine.dto.ShutDownPlanDTO;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.ShutDownPlanService;

@RestController
@RequestMapping("task")
public class ShutDownPlanController {

	@Autowired
	private ShutDownPlanService shutDownPlanService;

	@GetMapping(value = "/shutdown-plan")
	public ResponseEntity<AOPMessageVM> findMaintenanceDetailsByPlantIdAndType(@RequestParam String plantId,
			@RequestParam String maintenanceTypeName, @RequestParam String year) {
		
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<ShutDownPlanDTO> shutDownPlanDTOList = null;
		try {
			// Convert String to UUID
			UUID plantUuid = UUID.fromString(plantId);
			shutDownPlanDTOList = shutDownPlanService.findMaintenanceDetailsByPlantIdAndType(plantUuid,
					maintenanceTypeName, year);
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(shutDownPlanDTOList);
			
		
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(null);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body(null);
		}

		return ResponseEntity.status(aopMessageVM.getCode()).body(aopMessageVM);
	}
	
	@PostMapping(value = "/shutdown-plan/{plantId}")
	public ResponseEntity<AOPMessageVM> saveShutdownData(@PathVariable UUID plantId,
			@RequestBody List<ShutDownPlanDTO> shutDownPlanDTOList) {
		AOPMessageVM aopMessageVM =shutDownPlanService.saveShutdownPlantData(plantId, shutDownPlanDTOList);
		return ResponseEntity.status(aopMessageVM.getCode()).body(aopMessageVM);
	}

	@PutMapping(value = "/shutdown-plan/{plantMaintenanceTransactionId}")
	public ResponseEntity<List<ShutDownPlanDTO>> editShutdownData(@PathVariable UUID plantMaintenanceTransactionId,
			@RequestBody List<ShutDownPlanDTO> shutDownPlanDTOList) {
		shutDownPlanService.editShutdownData(plantMaintenanceTransactionId, shutDownPlanDTOList);
		return ResponseEntity.ok(shutDownPlanDTOList);
	}

	@DeleteMapping("/shutdown-plan/{plantMaintenanceTransactionId}")
	public ResponseEntity<String> deletePlant(@PathVariable UUID plantMaintenanceTransactionId) {
		shutDownPlanService.deletePlanData(plantMaintenanceTransactionId);
		return ResponseEntity.ok("Plant with ID " + plantMaintenanceTransactionId + " deleted successfully");
	}

	@GetMapping("/shutdown-plan/monthly-hours")
	public List<MonthWiseDataDTO> getMonthlyShutdownHours(@RequestParam String auditYear,
			@RequestParam String plantId) {
		return shutDownPlanService.getMonthlyShutdownHours(auditYear, UUID.fromString(plantId));
	}
}
