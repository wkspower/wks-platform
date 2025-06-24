package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import com.wks.caseengine.dto.TurnAroundPlanReportDTO;
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
	
	@PostMapping(value = "/report/turn-around")
	public ResponseEntity<AOPMessageVM> updateReportForTurnAroundData(@RequestParam String plantId,
			@RequestParam String year,@RequestParam String reportType,@RequestBody List<TurnAroundPlanReportDTO> dataList) {
		AOPMessageVM response = turnAroundDataReportService.updateReportForTurnAroundData(plantId, year,reportType, dataList);
		return ResponseEntity.status(response.getCode()).body(response);
	}

}
