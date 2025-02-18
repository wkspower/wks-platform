package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.service.SiteService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("site")
@Tag(name = "Site", description = "test Site")
public class SiteController {
	
	@Autowired
	private SiteService siteService;
	
	@GetMapping(value = "/getAllSites")
	public ResponseEntity<List<Sites>> getAllSites() {
		List<Sites> listOfSites = siteService.getAllSites(); 
	    return ResponseEntity.ok(listOfSites);
	}
	
	@GetMapping(value = "/getAllSitesAndPlants")
	public ResponseEntity<List<Object[]>> getAllSitesAndPlants() {
		List<Object[]> listOfSites = siteService.getAllSitesAndPlants(); 
		return ResponseEntity.ok(listOfSites);
	}

}
