package com.wks.caseengine.rest.server;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Date;
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
import com.wks.caseengine.dto.product.ShutDownPlanDTO;
import com.wks.caseengine.entity.PlantMaintenanceTransaction;
import com.wks.caseengine.service.ShutDownPlanService;



@RestController
@RequestMapping("task")
public class ShutDownPlanController {
	
	@Autowired
	private ShutDownPlanService shutDownPlanService;
	
	@GetMapping(value = "/getShutDownPlanData")
    public ResponseEntity<List<ShutDownPlanDTO>> findMaintenanceDetailsByPlantIdAndType(
            // Change UUID to String
            @RequestParam String plantId, 
            @RequestParam String maintenanceTypeName) {

        List<Object[]> listOfSite = new ArrayList<>();
        List<ShutDownPlanDTO> dtoList = new ArrayList<>();

        try {
            // Convert String to UUID
            UUID plantUuid = UUID.fromString(plantId); 
            listOfSite = shutDownPlanService.findMaintenanceDetailsByPlantIdAndType(plantUuid, maintenanceTypeName);

            for (Object[] result : listOfSite) {
                ShutDownPlanDTO dto = new ShutDownPlanDTO();
                dto.setDiscription((String) result[0]);
                dto.setMaintStartDateTime((Date) result[1]);
                dto.setMaintEndDateTime((Date) result[2]);
                dto.setDurationInMins(result[3] != null ? ((Integer) result[3]) : 0); 
                dto.setProduct((String) result[6]);
                //FOR ID : pmt.Id
                dto.setMaintenanceId(result[5] != null ? UUID.fromString(result[5].toString()) : null); 
                dtoList.add(dto);
            }
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null); 
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null); 
        }

        return ResponseEntity.ok(dtoList);
    }
	
		  @PostMapping(value = "/saveShutdownData/{plantId}")
            public ResponseEntity<ShutDownPlanDTO> saveShutdownData(@PathVariable UUID plantId, @RequestBody ShutDownPlanDTO shutDownPlanDTO) {
                // Log incoming request body
                
                //UUID plantMaintenanceId = planService.findPlantMaintenanceId(shutDownPlanDTO.getProduct());
			  	UUID plantMaintenanceId=shutDownPlanService.findIdByPlantIdAndMaintenanceTypeName(plantId,"Shutdown");
			  	System.out.println(plantMaintenanceId);


                PlantMaintenanceTransaction plantMaintenanceTransaction = new PlantMaintenanceTransaction();
                plantMaintenanceTransaction.setId(UUID.randomUUID());
                // Set mandatory fields with default values if missing
                plantMaintenanceTransaction.setDiscription(
                    shutDownPlanDTO.getDiscription() != null ? shutDownPlanDTO.getDiscription() : "Default Description"
                );
            
                plantMaintenanceTransaction.setDurationInMins(
                    shutDownPlanDTO.getDurationInMins() != null ? shutDownPlanDTO.getDurationInMins().intValue() : 0
                );

                
                plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintEndDateTime());
                plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintStartDateTime());
                // Ensure required fields exist


                plantMaintenanceTransaction.setUser("test_user"); 
                plantMaintenanceTransaction.setName("Default Name"); 
                plantMaintenanceTransaction.setVersion("V1"); 

                plantMaintenanceTransaction.setCreatedOn(new Date());

                
                if(shutDownPlanDTO.getProductId()!=null) {
                	plantMaintenanceTransaction.setNormParametersFKId(shutDownPlanDTO.getProductId());
                }



                plantMaintenanceTransaction.setPlantMaintenanceFkId(plantMaintenanceId);
                plantMaintenanceTransaction.setPlantFkId(plantId);


                
                // Save entity
                shutDownPlanService.saveShutdownData(plantMaintenanceTransaction);
                
                return ResponseEntity.ok(shutDownPlanDTO);
            }
		  
		  @PutMapping(value = "/editShutdownData/{plantMaintenanceTransactionId}")
          public ResponseEntity<ShutDownPlanDTO> editShutdownData(@PathVariable UUID plantMaintenanceTransactionId, @RequestBody ShutDownPlanDTO shutDownPlanDTO) {
              
			  PlantMaintenanceTransaction plantMaintenanceTransaction=shutDownPlanService.editShutDownPlanData(plantMaintenanceTransactionId);
			  plantMaintenanceTransaction.setDiscription(shutDownPlanDTO.getDiscription());
			  plantMaintenanceTransaction.setDurationInMins(shutDownPlanDTO.getDurationInMins());
			  plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintEndDateTime());
			  plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintStartDateTime());
			  plantMaintenanceTransaction.setNormParametersFKId(shutDownPlanDTO.getProductId());             
              // Save entity
              shutDownPlanService.saveShutdownData(plantMaintenanceTransaction);
              
              return ResponseEntity.ok(shutDownPlanDTO);
          }

		  @DeleteMapping("/deleteShutdownData/{plantMaintenanceTransactionId}")
		    public ResponseEntity<String> deletePlant(@PathVariable UUID plantMaintenanceTransactionId) {
			  	shutDownPlanService.deletePlanData(plantMaintenanceTransactionId);
		        return ResponseEntity.ok("Plant with ID " + plantMaintenanceTransactionId + " deleted successfully");
		    }
}
