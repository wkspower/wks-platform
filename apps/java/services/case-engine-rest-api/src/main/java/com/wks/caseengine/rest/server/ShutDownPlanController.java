package com.wks.caseengine.rest.server;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.product.ShutDownPlanDTO;
import com.wks.caseengine.service.ShutDownPlanService;

@RestController
@RequestMapping("task")
public class ShutDownPlanController {
	
	@Autowired
	private ShutDownPlanService planService;
	
	@GetMapping(value = "/getShutDownPlanData")
    public ResponseEntity<List<ShutDownPlanDTO>> findMaintenanceDetailsByPlantIdAndType(@RequestParam UUID plantId,@RequestParam String maintenanceTypeName) {
		List<Object[]> listOfSite=null;
		try {
			listOfSite = planService.findMaintenanceDetailsByPlantIdAndType(plantId,maintenanceTypeName);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
        List<ShutDownPlanDTO> dtoList = new ArrayList<>();
		
		  for (Object[] result : listOfSite) { 
			  ShutDownPlanDTO dto = new  ShutDownPlanDTO();
			  dto.setDiscription((String) result[0]); 
			  dto.setMaintStartDateTime((Date)result[1]);
			  dto.setMaintEndDateTime((Date) result[2]); 
			  dto.setDurationInMins((Long) result[3]);
			  dto.setProduct((String) result[4]);
			   
			  dtoList.add(dto); 
		}
		 
        return ResponseEntity.ok(dtoList);
    }


}
