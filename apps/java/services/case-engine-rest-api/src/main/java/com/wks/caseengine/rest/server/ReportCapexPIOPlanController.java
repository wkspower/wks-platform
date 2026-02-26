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
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.ReportCapexPIOPlanService;

@RestController
@RequestMapping("task")
public class ReportCapexPIOPlanController {

	@Autowired
	private ReportCapexPIOPlanService reportCapexPIOPlanService;
	
	@GetMapping(value="/report-capex-pioplan")
	public AOPMessageVM getReportCapexPIOPlanTransaction(@RequestParam String siteId,@RequestParam String year){
		 return  reportCapexPIOPlanService.getReportCapexPIOPlanTransaction(siteId,year);
	}
	
	@PostMapping(value="/report-capex-pioplan")
	public AOPMessageVM saveReportCapexPIOPlanTransaction(@RequestParam String year,@RequestParam String siteId, @RequestBody List<ReportCapexPIOPlanDTO> ReportCapexPIOPlanDTOs) {
		return 	reportCapexPIOPlanService.saveReportCapexPIOPlanTransaction(year,siteId,ReportCapexPIOPlanDTOs);
	}
	
}
