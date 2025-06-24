package com.wks.caseengine.rest.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.BasisReportService;

@RestController
@RequestMapping("task")
public class BasisReportController {
	
	@Autowired
	private BasisReportService basisReportService;
	
	@GetMapping(value="/report/norms-basis")
	public ResponseEntity<AOPMessageVM> getNormBasisReport(@RequestParam String plantId,@RequestParam String year,@RequestParam String type){
		AOPMessageVM response	=basisReportService.getNormBasisReport(plantId,year,type);
		return ResponseEntity.status(response.getCode()).body(response);
	}

}
