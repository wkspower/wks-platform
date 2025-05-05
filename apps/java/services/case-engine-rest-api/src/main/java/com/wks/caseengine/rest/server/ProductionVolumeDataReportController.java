package com.wks.caseengine.rest.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.ProductionVolumeDataReportService;

@RestController
@RequestMapping("task")
public class ProductionVolumeDataReportController {
	
	@Autowired
	private ProductionVolumeDataReportService productionVolumeDataReportService;
	
	@GetMapping(value="/report/production-summary")
	public ResponseEntity<AOPMessageVM> getReportForProductionVolumnData(@RequestParam String plantId,@RequestParam String year,@RequestParam(required = false) String type,@RequestParam(required = false) String filter){
		AOPMessageVM response	=productionVolumeDataReportService.getReportForProductionVolumnData(plantId,year,type,filter);
		return ResponseEntity.status(response.getCode()).body(response);
	}
}
