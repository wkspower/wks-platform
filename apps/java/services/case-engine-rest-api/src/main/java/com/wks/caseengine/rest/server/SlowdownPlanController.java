package com.wks.caseengine.rest.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.product.ShutDownPlanDTO;
import com.wks.caseengine.service.SlowdownPlanService;

@RestController
@RequestMapping("task")
public class SlowdownPlanController {
	
	@Autowired
	private SlowdownPlanService slowdownPlanService;
	
	@GetMapping(value = "/getSlowDownPlanData")
    public ResponseEntity<List<ShutDownPlanDTO>> findSlowdownDetailsByPlantIdAndType(@RequestParam UUID plantId,@RequestParam String maintenanceTypeName) {
		List<Object[]> listOfSite=null;
		try {
			listOfSite = slowdownPlanService.findSlowdownPlanDetailsByPlantIdAndType(plantId,maintenanceTypeName);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
        List<ShutDownPlanDTO> dtoList = new ArrayList<>();
		
		  for (Object[] result : listOfSite) { 
			  ShutDownPlanDTO dto = new  ShutDownPlanDTO();
			  dto.setDiscription((String) result[0]); 
			  dto.setMaintStartDateTime((Date)result[1]);
			  dto.setMaintEndDateTime((Date) result[2]); 
			  dto.setDurationInMins((Integer) result[3]);
			  dto.setRate((Double)result[4]);
			  dto.setRemark((String)result[5]);
			  dto.setProduct((String) result[6]);
			   
			  dtoList.add(dto); 
		}
		 
        return ResponseEntity.ok(dtoList);
    }


}
