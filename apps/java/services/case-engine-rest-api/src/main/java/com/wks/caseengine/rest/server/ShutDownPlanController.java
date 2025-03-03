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
import com.wks.caseengine.service.ShutDownPlanService;



@RestController
@RequestMapping("task")
public class ShutDownPlanController {
	
	@Autowired
	private ShutDownPlanService shutDownPlanService;
	
	@GetMapping(value = "/getShutDownPlanData")
    public ResponseEntity<List<ShutDownPlanDTO>> findMaintenanceDetailsByPlantIdAndType( @RequestParam String plantId, @RequestParam String maintenanceTypeName){
            
		List<ShutDownPlanDTO> shutDownPlanDTOList=null;
         try {
            // Convert String to UUID
            UUID plantUuid = UUID.fromString(plantId); 
            shutDownPlanDTOList = shutDownPlanService.findMaintenanceDetailsByPlantIdAndType(plantUuid, maintenanceTypeName);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null); 
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null); 
        }

        return ResponseEntity.ok(shutDownPlanDTOList);
    }
	
		  @PostMapping(value = "/saveShutdownData/{plantId}")
            public ResponseEntity<List<ShutDownPlanDTO>> saveShutdownData(@PathVariable UUID plantId, @RequestBody List<ShutDownPlanDTO> shutDownPlanDTOList) {
                shutDownPlanService.saveShutdownPlantData(plantId,shutDownPlanDTOList);
                return ResponseEntity.ok(shutDownPlanDTOList);
            }
		  
		  @PutMapping(value = "/editShutdownData/{plantMaintenanceTransactionId}")
          public ResponseEntity<List<ShutDownPlanDTO>> editShutdownData(@PathVariable UUID plantMaintenanceTransactionId, @RequestBody List<ShutDownPlanDTO> shutDownPlanDTOList) {
              shutDownPlanService.editShutdownData(plantMaintenanceTransactionId,shutDownPlanDTOList);
              return ResponseEntity.ok(shutDownPlanDTOList);
          }

		  @DeleteMapping("/deleteShutdownData/{plantMaintenanceTransactionId}")
		    public ResponseEntity<String> deletePlant(@PathVariable UUID plantMaintenanceTransactionId) {
			  	shutDownPlanService.deletePlanData(plantMaintenanceTransactionId);
		        return ResponseEntity.ok("Plant with ID " + plantMaintenanceTransactionId + " deleted successfully");
		    }
		  
		  @GetMapping("/getMonthlyShutdownHours")
		  public List<MonthWiseDataDTO> getMonthlyShutdownHours(@RequestParam String auditYear,@RequestParam String plantId){
			  return shutDownPlanService.getMonthlyShutdownHours(auditYear,UUID.fromString(plantId));
		  }
}
