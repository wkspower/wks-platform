package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.ReportCapexPIOPlanDTO;
import com.wks.caseengine.dto.ShutdownSlowdownPlanDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.ReportCapexPIOPlanService;
import com.wks.caseengine.service.ReportShutdownSlowdownPlanService;

@RestController
@RequestMapping("task")
public class ReportShutdownSlowdownPlanController {

	@Autowired
	private ReportShutdownSlowdownPlanService reportShutdownSlowdownPlanService;
	
	@GetMapping(value="/shutdown-slowdown-plan")
	public AOPMessageVM getShutdownSlowdownPlan(@RequestParam String siteId,@RequestParam String year){
		 return  reportShutdownSlowdownPlanService.getShutdownSlowdownPlan(siteId,year);
	}
	
	@PostMapping(value="/shutdown-slowdown-plan")
	public AOPMessageVM saveShutdownSlowdownPlan(@RequestParam String year,@RequestParam String siteId, @RequestBody List<ShutdownSlowdownPlanDTO> shutdownSlowdownPlanDTOs) {
		return 	reportShutdownSlowdownPlanService.saveShutdownSlowdownPlan(year,siteId,shutdownSlowdownPlanDTOs);
	}
	
}
