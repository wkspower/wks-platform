package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.rest.entity.Site;
import com.wks.caseengine.service.SiteService;

@RestController
public class SiteController {
	
	@Autowired
	private SiteService siteService;
	
	@GetMapping(value = "/getAllSites")
	public ResponseEntity<List<Site>> getAllSites() {
		List<Site> listOfSites = siteService.getAllSites(); 
	    return ResponseEntity.ok(listOfSites);
	}

}
