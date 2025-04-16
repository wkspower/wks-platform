package com.wks.caseengine.rest.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.PlantsDTO;
import com.wks.caseengine.dto.SitesDTO;
import com.wks.caseengine.dto.VerticalsDTO;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.PlantsService;
import com.wks.caseengine.service.SiteService;
import com.wks.caseengine.service.VerticalsService;

@RestController
@RequestMapping("api/verticals")
public class VerticalsController {
	
	@Autowired
	private PlantsService plantsService;
	
	@Autowired
	private SiteService siteService;
	
	@Autowired
	private VerticalsService verticalsService;
	
	@GetMapping
	public ResponseEntity<AOPMessageVM> getAllVerticals() {
		AOPMessageVM response = verticalsService.getAllVerticals();
		return ResponseEntity.status(response.getCode()).body(response);
	}
	
	@GetMapping(value="/plants")
	public ResponseEntity<AOPMessageVM> getPlantsAndSites() {
		AOPMessageVM response = verticalsService.getHierarchyData(); 
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
