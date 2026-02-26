package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.EnergyPerformanceDTO;
import com.wks.caseengine.dto.PerformanceHighlightDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

import com.wks.caseengine.service.PerformanceHighlightsService;

@RestController
@RequestMapping("task")
public class PerformanceHighlightsController {
	
	@Autowired
	private PerformanceHighlightsService performanceHighlightsService;
	
	@GetMapping(value="/performance-highlights")
	public AOPMessageVM getPerformanceHighlights(@RequestParam String siteId,@RequestParam String year){
		 return  performanceHighlightsService.getPerformanceHighlights(siteId,year);
	}
	
	@PostMapping(value="/performance-highlights")
	public AOPMessageVM savePerformanceHighlights(@RequestParam String year,@RequestParam String siteId, @RequestBody List<PerformanceHighlightDTO> performanceHighlightDTOs) {
		return 	performanceHighlightsService.savePerformanceHighlights(year,siteId,performanceHighlightDTOs);
	}
	
		
}
