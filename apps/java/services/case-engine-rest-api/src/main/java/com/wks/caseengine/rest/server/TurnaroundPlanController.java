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
import com.wks.caseengine.dto.ShutDownPlanDTO;
import com.wks.caseengine.service.ShutDownPlanService;
import com.wks.caseengine.service.TurnaroundPlanService;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("task")
public class TurnaroundPlanController {
	
	@Autowired
	private ShutDownPlanService shutDownPlanService;
	
	@Autowired
	private TurnaroundPlanService turnaroundPlanService;
	
	@GetMapping(value = "/getTurnaroundPlanData")
    public ResponseEntity<List<ShutDownPlanDTO>> findTurnaroundPlanDataByPlantIdAndType(@RequestParam UUID plantId,@RequestParam String maintenanceTypeName,@RequestParam String year) {
		
		List<ShutDownPlanDTO> listOfSite=null;
		try {
			listOfSite = turnaroundPlanService.findTurnaroundPlanDataByPlantIdAndType(plantId,maintenanceTypeName,year);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
        
		 
        return ResponseEntity.ok(listOfSite);
    }
	
	@PostMapping(value="/saveTurnaroundPlanData/{plantId}")
	public ResponseEntity<List<ShutDownPlanDTO>> saveShutdownData(@PathVariable UUID plantId,@RequestBody List<ShutDownPlanDTO> shutDownPlanDTOList){
		turnaroundPlanService.saveTurnaroundPlanData(plantId,shutDownPlanDTOList);
		return ResponseEntity.ok(shutDownPlanDTOList); 
	}
	
	@PutMapping(value = "/editTurnaroundData/{plantMaintenanceTransactionId}")
    public ResponseEntity<List<ShutDownPlanDTO>> editShutdownData(@PathVariable UUID plantMaintenanceTransactionId, @RequestBody List<ShutDownPlanDTO> shutDownPlanDTOList) {
        
		          // Save entity
		turnaroundPlanService.editTurnaroundPlanData(plantMaintenanceTransactionId,shutDownPlanDTOList);
        
        return ResponseEntity.ok(shutDownPlanDTOList);
    }
	
	@DeleteMapping("/deleteTurnaroundData/{plantMaintenanceTransactionId}")
    public ResponseEntity<String> deletePlant(@PathVariable UUID plantMaintenanceTransactionId,@PathVariable UUID plantId) {
	  	shutDownPlanService.deletePlanData(plantMaintenanceTransactionId,plantId);
        return ResponseEntity.ok("Plant with ID " + plantMaintenanceTransactionId + " deleted successfully");
    }

}
