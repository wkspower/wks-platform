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

import com.wks.caseengine.dto.product.SlowDownPlanDTO;
import com.wks.caseengine.dto.product.ShutDownPlanDTO;
import com.wks.caseengine.entity.PlantMaintenanceTransaction;
import com.wks.caseengine.service.ShutDownPlanService;
import com.wks.caseengine.service.SlowdownPlanService;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("task")
public class SlowdownPlanController {
	
	@Autowired
	private SlowdownPlanService slowdownPlanService;
	
	@Autowired
	private ShutDownPlanService shutDownPlanService;
	
	@GetMapping(value = "/getSlowDownPlanData")
    public ResponseEntity<List<SlowDownPlanDTO>> findSlowdownDetailsByPlantIdAndType(@RequestParam UUID plantId,@RequestParam String maintenanceTypeName) {
		List<Object[]> listOfSite=null;
		try {
			listOfSite = slowdownPlanService.findSlowdownPlanDetailsByPlantIdAndType(plantId,maintenanceTypeName);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
        List<SlowDownPlanDTO> dtoList = new ArrayList<>();
		
		for (Object[] result : listOfSite) {
			SlowDownPlanDTO dto = new SlowDownPlanDTO();
			dto.setDiscription((String) result[0]);
			dto.setMaintStartDateTime((Date) result[1]);
			dto.setMaintEndDateTime((Date) result[2]);
			dto.setDurationInMins(result[3] != null ? ((Number) result[3]).longValue() : 0L);
			dto.setRate(result[4] != null ? ((Number) result[4]).doubleValue() : null); // Extract Rate
			dto.setRemarks(result[5] != null ? result[5].toString() : null); // Extract Remarks
			dto.setProduct(result[8] != null ? result[8].toString() : null);
			dto.setMaintenanceId(result[7] != null ? UUID.fromString(result[7].toString()) : null);
	
			dtoList.add(dto);
		}
		 
        return ResponseEntity.ok(dtoList);
    }
	
	@PostMapping(value="/saveSlowdownData/{plantId}")
	public ResponseEntity<ShutDownPlanDTO> saveShutdownData(@PathVariable UUID plantId,@RequestBody ShutDownPlanDTO shutDownPlanDTO){
		UUID plantMaintenanceId=shutDownPlanService.findIdByPlantIdAndMaintenanceTypeName(plantId,"Slowdown");
		PlantMaintenanceTransaction plantMaintenanceTransaction=new PlantMaintenanceTransaction();
		plantMaintenanceTransaction.setId(UUID.randomUUID());



		plantMaintenanceTransaction.setDiscription(shutDownPlanDTO.getDiscription());

		// plantMaintenanceTransaction.setDurationInMins(shutDownPlanDTO.getDurationInMins());
		plantMaintenanceTransaction.setDurationInMins(shutDownPlanDTO.getDurationInMins().intValue());


		plantMaintenanceTransaction.setName("Default Name"); // Add default name
        plantMaintenanceTransaction.setVersion("V1"); // Ensure version is set
		plantMaintenanceTransaction.setUser("test_user"); 

		plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintEndDateTime());
		plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintStartDateTime());
		plantMaintenanceTransaction.setPlantMaintenanceFkId(plantMaintenanceId);
		plantMaintenanceTransaction.setPlantFkId(plantId);


		plantMaintenanceTransaction.setCreatedOn(new Date());


		if(shutDownPlanDTO.getProductId()!=null) {
			plantMaintenanceTransaction.setNormParametersFKId(shutDownPlanDTO.getProductId());
		}
		
		
		// plantMaintenanceTransaction.setRate(shutDownPlanDTO.getRate());
		plantMaintenanceTransaction.setRate(shutDownPlanDTO.getRate());

		plantMaintenanceTransaction.setRemarks(shutDownPlanDTO.getRemark());
		shutDownPlanService.saveShutdownData(plantMaintenanceTransaction);
		return ResponseEntity.ok(shutDownPlanDTO); 
	}
	
	@PutMapping(value = "/editSlowdownData/{plantMaintenanceTransactionId}")
    public ResponseEntity<ShutDownPlanDTO> editShutdownData(@PathVariable UUID plantMaintenanceTransactionId, @RequestBody ShutDownPlanDTO shutDownPlanDTO) {
        
		  PlantMaintenanceTransaction plantMaintenanceTransaction=shutDownPlanService.editShutDownPlanData(plantMaintenanceTransactionId);
		  plantMaintenanceTransaction.setDiscription(shutDownPlanDTO.getDiscription());
		  plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintEndDateTime());
		  plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintStartDateTime());
		  plantMaintenanceTransaction.setNormParametersFKId(shutDownPlanDTO.getProductId());
		  plantMaintenanceTransaction.setRate(shutDownPlanDTO.getRate());
		  plantMaintenanceTransaction.setRemarks(shutDownPlanDTO.getRemark());
        // Save entity
        shutDownPlanService.saveShutdownData(plantMaintenanceTransaction);
        
        return ResponseEntity.ok(shutDownPlanDTO);
    }
	
	@DeleteMapping("/deleteSlowdownData/{plantMaintenanceTransactionId}")
    public ResponseEntity<String> deletePlant(@PathVariable UUID plantMaintenanceTransactionId) {
	  	shutDownPlanService.deletePlanData(plantMaintenanceTransactionId);
        return ResponseEntity.ok("Plant with ID " + plantMaintenanceTransactionId + " deleted successfully");
    }
	
}
