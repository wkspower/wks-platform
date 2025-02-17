package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.wks.caseengine.service.PlantService;
import com.wks.caseengine.rest.entity.Plant;

@RestController
public class PlantController {
	
	@Autowired
	private PlantService PlantService;
	
	@GetMapping(value = "/getPlantBySite")
	public ResponseEntity<List<Plant>> getPlantBySite(@RequestParam String siteId) {
		List<Plant> listOfSite = PlantService.getPlantBySite(siteId); 
	    return ResponseEntity.ok(listOfSite);
	}

}
