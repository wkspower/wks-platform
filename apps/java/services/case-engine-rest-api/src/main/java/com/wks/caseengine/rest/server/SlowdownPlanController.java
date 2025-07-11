package com.wks.caseengine.rest.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
import com.wks.caseengine.dto.NormAttributeTransactionsDTO;
import com.wks.caseengine.dto.ShutDownPlanDTO;
import com.wks.caseengine.entity.PlantMaintenanceTransaction;
import com.wks.caseengine.message.vm.AOPMessageVM;
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
    public ResponseEntity<List<ShutDownPlanDTO>> findSlowdownDetailsByPlantIdAndType(@RequestParam UUID plantId,@RequestParam String maintenanceTypeName, @RequestParam String year) {
		List<ShutDownPlanDTO> listOfSite=null;
		try {
			listOfSite = slowdownPlanService.findSlowdownDetailsByPlantIdAndType(plantId,maintenanceTypeName,year);
		}catch(Exception e) {
			e.printStackTrace();
		}
        return ResponseEntity.ok(listOfSite);
    }
	
	@PostMapping(value="/saveSlowdownData/{plantId}")
	public ResponseEntity<List<ShutDownPlanDTO>> saveShutdownData(@PathVariable UUID plantId,@RequestBody List<ShutDownPlanDTO> shutDownPlanDTOList){
		slowdownPlanService.saveShutdownData(plantId,shutDownPlanDTOList);
				return ResponseEntity.ok(shutDownPlanDTOList); 
	}
	
	@PutMapping(value = "/editSlowdownData/{plantMaintenanceTransactionId}")
    public ResponseEntity<List<ShutDownPlanDTO>> editShutdownData(@PathVariable UUID plantMaintenanceTransactionId, @RequestBody List<ShutDownPlanDTO> shutDownPlanDTOList) {
        
		slowdownPlanService.editShutdownData(plantMaintenanceTransactionId,shutDownPlanDTOList);
		          
        return ResponseEntity.ok(shutDownPlanDTOList);
    }
	
	@DeleteMapping("/deleteSlowdownData/{plantMaintenanceTransactionId}/{plantId}")
    public ResponseEntity<String> deletePlant(@PathVariable UUID plantMaintenanceTransactionId,@PathVariable UUID plantId) {
	  	shutDownPlanService.deletePlanData(plantMaintenanceTransactionId,plantId);
        return ResponseEntity.ok("Plant with ID " + plantMaintenanceTransactionId + " deleted successfully");
    }
	
	@PostMapping(value="/slowdown/configuration")
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
	
	@GetMapping(value = "/slowdown/configuration")
    public AOPMessageVM getSlowdownConfigurationData(@RequestParam String plantId, @RequestParam String year) {
		
		try {
			return slowdownPlanService.getSlowdownConfigurationData(plantId,year);
		}catch(Exception e) {
			e.printStackTrace();
		}
        return null;
    }
	
	@GetMapping("/shutdown/dynamic/columns")
	  public AOPMessageVM getShutdownDynamicColumns(@RequestParam String year,@RequestParam String plantId){
		  return slowdownPlanService.getShutdownDynamicColumns(year,UUID.fromString(plantId));
	  }
	
}
