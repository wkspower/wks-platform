package com.wks.caseengine.rest.server;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestBody;

import com.wks.caseengine.dto.MonthWiseDataDTO;
import com.wks.caseengine.dto.ShutDownPlanDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.ShutDownPlanService;



@RestController
@RequestMapping("task")
public class ShutDownPlanController {
	
	@Autowired
	private ShutDownPlanService shutDownPlanService;
	
	@GetMapping(value = "/shutdown")
    public ResponseEntity<List<ShutDownPlanDTO>> findMaintenanceDetailsByPlantIdAndType( @RequestParam String plantId, @RequestParam String maintenanceTypeName, @RequestParam String year){
            
		List<ShutDownPlanDTO> shutDownPlanDTOList=null;
         try {
            // Convert String to UUID
            UUID plantUuid = UUID.fromString(plantId); 
            shutDownPlanDTOList = shutDownPlanService.findMaintenanceDetailsByPlantIdAndType(plantUuid, maintenanceTypeName,year);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null); 
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null); 
        }

        return ResponseEntity.ok(shutDownPlanDTOList);
    }
	
	@GetMapping(value = "/shutdown-export")
	public ResponseEntity<byte[]> shutdownExport(
	         @RequestParam String year,@RequestParam String plantId,@RequestParam String maintenanceTypeName) {
	    try {
			
	        byte[] excelBytes = shutDownPlanService.shutdownExport(year, plantId,maintenanceTypeName, false, null);

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("shutdown.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	@GetMapping(value = "/shutdown-export-non-product")
	public ResponseEntity<byte[]> shutdownNonProductExport(
	         @RequestParam String year,@RequestParam String plantId,@RequestParam String maintenanceTypeName) {
	    try {
			
	        byte[] excelBytes = shutDownPlanService.shutdownNonProductExport(year, plantId,maintenanceTypeName, false, null);

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("shutdown.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	@PostMapping(value = "/shutdown-import", consumes = "multipart/form-data")
	public AOPMessageVM importShutdownExcel(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,@RequestParam String maintenanceTypeName,
			@RequestParam("file") MultipartFile file
	        ) {
			return	shutDownPlanService.importShutdownExcel(year,UUID.fromString(plantId),  maintenanceTypeName, file); 
	}
	
	@PostMapping(value = "/shutdown-import-non-product", consumes = "multipart/form-data")
	public AOPMessageVM importNonProductShutdown(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,@RequestParam String maintenanceTypeName,
			@RequestParam("file") MultipartFile file
	        ) {
			return	shutDownPlanService.importNonProductShutdown(year,UUID.fromString(plantId),  maintenanceTypeName, file); 
	}

	
		  @PostMapping(value = "/shutdown/{plantId}")
            public ResponseEntity<List<ShutDownPlanDTO>> saveShutdownData(@PathVariable UUID plantId, @RequestBody List<ShutDownPlanDTO> shutDownPlanDTOList) {
                shutDownPlanService.saveShutdownPlantData(plantId,shutDownPlanDTOList);
                return ResponseEntity.ok(shutDownPlanDTOList);
            }
		  
		  @PutMapping(value = "/editShutdownData/{plantMaintenanceTransactionId}")
          public ResponseEntity<List<ShutDownPlanDTO>> editShutdownData(@PathVariable UUID plantMaintenanceTransactionId, @RequestBody List<ShutDownPlanDTO> shutDownPlanDTOList) {
              shutDownPlanService.editShutdownData(plantMaintenanceTransactionId,shutDownPlanDTOList);
              return ResponseEntity.ok(shutDownPlanDTOList);
          }

		  @DeleteMapping("/shutdown/{plantMaintenanceTransactionId}/{plantId}")
		    public ResponseEntity<String> deletePlant(@PathVariable UUID plantMaintenanceTransactionId,@PathVariable UUID plantId) {	
			  shutDownPlanService.deleteShutPlanData(plantMaintenanceTransactionId,plantId);
		        return ResponseEntity.ok("Plant with ID " + plantMaintenanceTransactionId + " deleted successfully");
		    }
		  
		  @GetMapping("/getMonthlyShutdownHours")
		  public List<MonthWiseDataDTO> getMonthlyShutdownHours(@RequestParam String auditYear,@RequestParam String plantId){
			  return shutDownPlanService.getMonthlyShutdownHours(auditYear,UUID.fromString(plantId));
		  }
		  
		  @GetMapping("/description-drpdwn")
		  public AOPMessageVM getDescriptionDropdown(@RequestParam String plantId){
			  return shutDownPlanService.getDescriptionDropdown(plantId);
		  }
		  
		  
}
