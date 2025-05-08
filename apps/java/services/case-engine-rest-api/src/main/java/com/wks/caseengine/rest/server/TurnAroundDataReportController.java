package com.wks.caseengine.rest.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.TurnAroundDataReportService;

@RestController
@RequestMapping("task")
public class TurnAroundDataReportController {
	
	@Autowired
	private TurnAroundDataReportService turnAroundDataReportService;
	
	@GetMapping(value="/report/turn-around")
	public ResponseEntity<AOPMessageVM> getReportForTurnAroundData(@RequestParam String plantId,@RequestParam String year,@RequestParam String reportType){
		AOPMessageVM response	= turnAroundDataReportService.getReportForTurnAroundPlanData(plantId,year,reportType);
		return ResponseEntity.status(response.getCode()).body(response);
	}

}
