package com.wks.caseengine.rest.server;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.AOPReportService;

@RestController
@RequestMapping("task")
public class AOPReportController {
	
	@Autowired
	private AOPReportService aopReportService;
	
	@GetMapping(value="/report/annual-aop")
	public ResponseEntity<AOPMessageVM> getAnnualAOPReport(@RequestParam String plantId,@RequestParam String year,@RequestParam String reportType,@RequestParam String aopYearFilter){
		AOPMessageVM response	=aopReportService.getAnnualAOPReport(plantId,year,reportType,aopYearFilter);
		return ResponseEntity.status(response.getCode()).body(response);
	}


}
