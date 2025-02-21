package com.wks.caseengine.rest.server;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.product.ShutDownPlanDTO;
import com.wks.caseengine.entity.PlantMaintenanceTransaction;
import com.wks.caseengine.service.ShutDownPlanService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("task")
public class ShutDownPlanController {
	
	@Autowired
	private ShutDownPlanService planService;
	
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
            listOfSite = planService.findMaintenanceDetailsByPlantIdAndType(plantUuid, maintenanceTypeName);

            for (Object[] result : listOfSite) {
                ShutDownPlanDTO dto = new ShutDownPlanDTO();
                dto.setDiscription((String) result[0]);
                dto.setMaintStartDateTime((Date) result[1]);
                dto.setMaintEndDateTime((Date) result[2]);
                dto.setDurationInMins(result[3] != null ? ((Number) result[3]).longValue() : 0L); 
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
                
            
                UUID plantMaintenanceId = planService.findPlantMaintenanceId(shutDownPlanDTO.getProduct());
                PlantMaintenanceTransaction plantMaintenanceTransaction = new PlantMaintenanceTransaction();
                plantMaintenanceTransaction.setId(UUID.randomUUID());
            
                // Set mandatory fields with default values if missing
                plantMaintenanceTransaction.setDiscription(
                    shutDownPlanDTO.getDiscription() != null ? shutDownPlanDTO.getDiscription() : "Default Description"
                );
            
                plantMaintenanceTransaction.setDurationInMins(
                    shutDownPlanDTO.getDurationInMins() != null ? shutDownPlanDTO.getDurationInMins().intValue() : 0
                );

                plantMaintenanceTransaction.setUser("test_user"); 

                
                System.out.println("1: " + shutDownPlanDTO.getMaintEndDateTime());
                System.out.println("2: " + shutDownPlanDTO.getMaintStartDateTime());
            
                plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintEndDateTime());


               
                plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintStartDateTime());


            
                // Ensure required fields exist
                plantMaintenanceTransaction.setName("Default Name"); // Add default name
                plantMaintenanceTransaction.setVersion("V1"); // Ensure version is set

                plantMaintenanceTransaction.setCreatedOn(new Date());
            
                plantMaintenanceTransaction.setPlantMaintenanceFkId(plantMaintenanceId);
                plantMaintenanceTransaction.setPlantFkId(plantId);
            
                // Save entity
                planService.saveShutdownData(plantMaintenanceTransaction);
                
                return ResponseEntity.ok(shutDownPlanDTO);
            }

}
