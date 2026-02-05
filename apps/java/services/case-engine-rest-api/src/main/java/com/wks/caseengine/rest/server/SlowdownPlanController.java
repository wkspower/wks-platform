package com.wks.caseengine.rest.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
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

import com.wks.caseengine.dto.SlowDownPlanDTO;
import com.wks.caseengine.dto.NormAttributeTransactionsDTO;
import com.wks.caseengine.dto.ShutDownPlanDTO;
import com.wks.caseengine.entity.PlantMaintenanceTransaction;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.VerticalsRepository;
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
	
	@Autowired
	private PlantsRepository plantsRepository;
	
	@Autowired
	private VerticalsRepository verticalRepository;
	
	@GetMapping(value = "/slowdown")
    public ResponseEntity<List<ShutDownPlanDTO>> findSlowdownDetailsByPlantIdAndType(@RequestParam UUID plantId,@RequestParam String maintenanceTypeName, @RequestParam String year) {
		List<ShutDownPlanDTO> listOfSite=null;
		try {
			listOfSite = slowdownPlanService.findSlowdownDetailsByPlantIdAndType(plantId,maintenanceTypeName,year);
		}catch(Exception e) {
			e.printStackTrace();
		}
        return ResponseEntity.ok(listOfSite);
    }
	
	@GetMapping(value = "/slowdown-export")
	public ResponseEntity<byte[]> slowdownExport(
	         @RequestParam String year,@RequestParam String plantId,@RequestParam String maintenanceTypeName) {
	    try {
	    	byte[] excelBytes=null;
	    	Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
	        Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			if(vertical.getName().equalsIgnoreCase("PE") || vertical.getName().equalsIgnoreCase("PP") || vertical.getName().equalsIgnoreCase("PET")) {
				 excelBytes = slowdownPlanService.slowdownExportPE(year, plantId,maintenanceTypeName, false, null);
			}else {
				  excelBytes = slowdownPlanService.slowdownExport(year, plantId,maintenanceTypeName, false, null);
			}
	       

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("slowdown.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	@GetMapping(value = "/slowdown-rate-export")
	public ResponseEntity<byte[]> slowdownRateExport(
	         @RequestParam String year,@RequestParam String plantId,@RequestParam String maintenanceTypeName) {
	    try {
			
	        byte[] excelBytes = slowdownPlanService.slowdownRateExport(year, plantId,maintenanceTypeName, false, null);

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("slowdown.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	@GetMapping(value = "/slowdown-export-non-product")
	public ResponseEntity<byte[]> nonProductSlowdownExport(
	         @RequestParam String year,@RequestParam String plantId,@RequestParam String maintenanceTypeName) {
	    try {
			
	        byte[] excelBytes = slowdownPlanService.nonProductSlowdownExport(year, plantId,maintenanceTypeName, false, null);

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("slowdown.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}

	
	@PostMapping(value = "/slowdown-import", consumes = "multipart/form-data")
	public AOPMessageVM importSlowdownExcel(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,@RequestParam String maintenanceTypeName,
			@RequestParam("file") MultipartFile file
	        ) {
			return	slowdownPlanService.importSlowdownExcel(year,UUID.fromString(plantId),  maintenanceTypeName, file); 
	}
	
	@PostMapping(value = "/slowdown-rate-import", consumes = "multipart/form-data")
	public AOPMessageVM importSlowdownRateExcel(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,@RequestParam String maintenanceTypeName,
			@RequestParam("file") MultipartFile file
	        ) {
			return	slowdownPlanService.importSlowdownRateExcel(year,UUID.fromString(plantId),  maintenanceTypeName, file); 
	}
	
	@PostMapping(value = "/slowdown-import-non-product", consumes = "multipart/form-data")
	public AOPMessageVM importNonProductSlowdown(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,@RequestParam String maintenanceTypeName,
			@RequestParam("file") MultipartFile file
	        ) {
			return	slowdownPlanService.importNonProductSlowdown(year,UUID.fromString(plantId),  maintenanceTypeName, file); 
	}

	
	@PostMapping(value="/slowdown/{plantId}")
	public ResponseEntity<List<ShutDownPlanDTO>> saveShutdownData(@PathVariable UUID plantId,@RequestBody List<ShutDownPlanDTO> shutDownPlanDTOList){
		slowdownPlanService.saveShutdownData(plantId,shutDownPlanDTOList);
				return ResponseEntity.ok(shutDownPlanDTOList); 
	}
	
	@PutMapping(value = "/editSlowdownData/{plantMaintenanceTransactionId}")
    public ResponseEntity<List<ShutDownPlanDTO>> editShutdownData(@PathVariable UUID plantMaintenanceTransactionId, @RequestBody List<ShutDownPlanDTO> shutDownPlanDTOList) {
        
		slowdownPlanService.editShutdownData(plantMaintenanceTransactionId,shutDownPlanDTOList);
		          
        return ResponseEntity.ok(shutDownPlanDTOList);
    }
	
	@DeleteMapping("/slowdown/{plantMaintenanceTransactionId}/{plantId}")
    public ResponseEntity<String> deletePlant(@PathVariable UUID plantMaintenanceTransactionId,@PathVariable UUID plantId) {
	  	shutDownPlanService.deletePlanData(plantMaintenanceTransactionId,plantId);
        return ResponseEntity.ok("Plant with ID " + plantMaintenanceTransactionId + " deleted successfully");
    }
	
	@PostMapping(value="/slowdown-configuration")
	public AOPMessageVM saveSlowdownConfigurationData(@RequestParam String plantId,@RequestParam String year, @RequestBody List<Map<String, Object>> payload){
		List<NormAttributeTransactionsDTO> dtoList = new ArrayList<>();

	    for (Map<String, Object> item : payload) {
	    	 UUID normParameterId = UUID.fromString(item.get("normParameterFKId").toString());

	        for (Map.Entry<String, Object> entry : item.entrySet()) {
	            String key = entry.getKey();

	            if (!"normParameterFKId".equals(key)) {
	                Object value = entry.getValue();
	                
	                NormAttributeTransactionsDTO dto = new NormAttributeTransactionsDTO();

	                dto.setNormParameterFKId(normParameterId); 
	                dto.setDescription(key);
	                if(value!=null) {
	                	dto.setAttributeValue(value.toString());   
	                }
	                        
	                dtoList.add(dto);
	            }
	        }
	    }
		
		return slowdownPlanService.saveSlowdownConfigurationData(plantId,year,dtoList);		
	}
	
	@GetMapping(value = "/slowdown-configuration")
    public AOPMessageVM getSlowdownConfigurationData(@RequestParam String plantId, @RequestParam String year) {
		
		try {
			return slowdownPlanService.getSlowdownConfigurationData(plantId,year);
		}catch(Exception e) {
			e.printStackTrace();
		}
        return null;
    }
	
	@GetMapping("/slowdown-columns")
	  public AOPMessageVM getShutdownDynamicColumns(@RequestParam String year,@RequestParam String plantId){
		  return slowdownPlanService.getShutdownDynamicColumns(year,UUID.fromString(plantId));
	  }
	
	 @GetMapping("/slowdown-description")
	  public AOPMessageVM getSlowdownDescription(@RequestParam String plantId){
		  return slowdownPlanService.getSlowdownDescription(plantId);
	  }
	
}
