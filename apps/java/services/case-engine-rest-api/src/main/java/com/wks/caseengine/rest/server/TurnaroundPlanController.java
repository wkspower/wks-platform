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
import com.wks.caseengine.dto.product.ShutDownPlanDTO;
import com.wks.caseengine.entity.PlantMaintenanceTransaction;
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
    public ResponseEntity<List<ShutDownPlanDTO>> findTurnaroundPlanDataByPlantIdAndType(@RequestParam UUID plantId,@RequestParam String maintenanceTypeName) {
		List<Object[]> listOfSite=null;
		try {
			listOfSite = turnaroundPlanService.findTurnaroundPlanDetailsByPlantIdAndType(plantId,maintenanceTypeName);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
        List<ShutDownPlanDTO> dtoList = new ArrayList<>();
		
		  for (Object[] result : listOfSite) { 
			  ShutDownPlanDTO dto = new  ShutDownPlanDTO();
			  dto.setDiscription((String) result[0]); 
			  dto.setMaintStartDateTime((Date)result[1]);
			  dto.setMaintEndDateTime((Date) result[2]);
			  // dto.setDurationInMins((Integer) result[3]); // Duration in minutes
			  dto.setDurationInMins(result[3] != null ? ((Integer) result[3]) : 0); 
			  double durationInHrs = ((Integer) result[3]) / 60.0;
			  dto.setDurationInHrs(durationInHrs);
			  dto.setRemark((String)result[4]);
			  dto.setProduct((String) result[7]);
			  long diffInMillis = dto.getMaintEndDateTime().getTime() - dto.getMaintStartDateTime().getTime();
			  double diffInDays = diffInMillis / (1000.0 * 60 * 60 * 24);
			  dto.setDurationInDays(diffInDays);
			  dto.setMaintenanceId(result[6] != null ? UUID.fromString(result[6].toString()) : null); 
			  dtoList.add(dto); 
		}
		 
        return ResponseEntity.ok(dtoList);
    }
	
	@PostMapping(value="/saveTurnaroundPlanData/{plantId}")
	public ResponseEntity<ShutDownPlanDTO> saveShutdownData(@PathVariable UUID plantId,@RequestBody ShutDownPlanDTO shutDownPlanDTO){
		UUID plantMaintenanceId=shutDownPlanService.findIdByPlantIdAndMaintenanceTypeName(plantId,"TA_Plan");
		PlantMaintenanceTransaction plantMaintenanceTransaction=new PlantMaintenanceTransaction();
		plantMaintenanceTransaction.setId(UUID.randomUUID());
		plantMaintenanceTransaction.setDiscription(shutDownPlanDTO.getDiscription());
		plantMaintenanceTransaction.setDurationInMins(shutDownPlanDTO.getDurationInMins().intValue());
		plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintEndDateTime());
		plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintStartDateTime());


		plantMaintenanceTransaction.setUser("test_user"); 
		plantMaintenanceTransaction.setName("Default Name"); 
		plantMaintenanceTransaction.setVersion("V1"); 

		plantMaintenanceTransaction.setRemarks(shutDownPlanDTO.getRemark());


		if(shutDownPlanDTO.getProductId()!=null) {
			plantMaintenanceTransaction.setNormParametersFKId(shutDownPlanDTO.getProductId());
		}


		plantMaintenanceTransaction.setCreatedOn(new Date());

		plantMaintenanceTransaction.setPlantMaintenanceFkId(plantMaintenanceId);
		plantMaintenanceTransaction.setPlantFkId(plantId);




		shutDownPlanService.saveShutdownData(plantMaintenanceTransaction);
		return ResponseEntity.ok(shutDownPlanDTO); 
	}
	
	@PutMapping(value = "/editTurnaroundData/{plantMaintenanceTransactionId}")
    public ResponseEntity<ShutDownPlanDTO> editShutdownData(@PathVariable UUID plantMaintenanceTransactionId, @RequestBody ShutDownPlanDTO shutDownPlanDTO) {
        
		  PlantMaintenanceTransaction plantMaintenanceTransaction=shutDownPlanService.editShutDownPlanData(plantMaintenanceTransactionId);
		  plantMaintenanceTransaction.setDiscription(shutDownPlanDTO.getDiscription());


		  plantMaintenanceTransaction.setDurationInMins(shutDownPlanDTO.getDurationInMins());

		  plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintEndDateTime());
		  plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintStartDateTime());
		  plantMaintenanceTransaction.setNormParametersFKId(shutDownPlanDTO.getProductId());
		  plantMaintenanceTransaction.setRemarks(shutDownPlanDTO.getRemark());
	        // Save entity
	        shutDownPlanService.saveShutdownData(plantMaintenanceTransaction);
        
        return ResponseEntity.ok(shutDownPlanDTO);
    }
	
	@DeleteMapping("/deleteTurnaroundData/{plantMaintenanceTransactionId}")
    public ResponseEntity<String> deletePlant(@PathVariable UUID plantMaintenanceTransactionId) {
	  	shutDownPlanService.deletePlanData(plantMaintenanceTransactionId);
        return ResponseEntity.ok("Plant with ID " + plantMaintenanceTransactionId + " deleted successfully");
    }

}
