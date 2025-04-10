package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.wks.caseengine.service.PlantService;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.rest.entity.Plant;

@RestController
public class PlantController {
	
	@Autowired
	private PlantService PlantService;
	
	@GetMapping(value = "/plants")
	public  ResponseEntity<AOPMessageVM> getPlantBySite(@RequestParam String siteId) {
		AOPMessageVM response = PlantService.getPlantBySite(siteId); 
		return ResponseEntity.status(response.getCode()).body(response);
	}

}
