package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.wks.caseengine.dto.ShutdownSlowdownPlanDTO;
import com.wks.caseengine.dto.TechnicalAvailabilityDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.ReportTechnicalAvailabilityService;

@RestController
@RequestMapping("task")
public class ReportTechnicalAvailabilityController {

	@Autowired
	private ReportTechnicalAvailabilityService reportTechnicalAvailabilityService;
	
	@GetMapping(value="/technical-availability")
	public AOPMessageVM getTechnicalAvailability(@RequestParam String siteId,@RequestParam String year){
		 return  reportTechnicalAvailabilityService.getTechnicalAvailability(siteId,year);
	}
	
	@PostMapping(value="/technical-availability")
	public AOPMessageVM saveShutdownSlowdownPlan(@RequestParam String year,@RequestParam String siteId, @RequestBody List<TechnicalAvailabilityDTO> technicalAvailabilityDTOs) {
		return 	reportTechnicalAvailabilityService.saveTechnicalAvailability(year,siteId,technicalAvailabilityDTOs);
	}
	
}
