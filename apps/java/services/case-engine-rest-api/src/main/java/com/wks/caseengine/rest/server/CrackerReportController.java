package com.wks.caseengine.rest.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.CrackerReportService;

@RestController
@RequestMapping("task")
public class CrackerReportController {
	
	@Autowired
	private CrackerReportService crackerReportService;
	
	@GetMapping(value="/spyro-input-report")
	public AOPMessageVM getSpyroInputReport(@RequestParam String plantId,@RequestParam String year, @RequestParam String mode) {
		return crackerReportService.getSpyroInputReport(plantId,year,mode);
	}
	
	@GetMapping(value="/spyro-output-report")
	public AOPMessageVM getSpyroOutputReport(@RequestParam String plantId,@RequestParam String year, @RequestParam String mode) {
		return crackerReportService.getSpyroOutputReport(plantId,year,mode);
	}
	
	@GetMapping(value="/final-norms-report")
	public AOPMessageVM getFinalNormsReport(@RequestParam String plantId,@RequestParam String year) {
		return crackerReportService.getFinalNormsReport(plantId,year);
	}
	
	@GetMapping(value="/final-norms-production-report")
	public AOPMessageVM getFinalNormsProductionReport(@RequestParam String plantId,@RequestParam String year) {
		return crackerReportService.getFinalNormsProductionReport(plantId,year);
	}
	
	@GetMapping(value="/configuration-intermediate-values")
	public AOPMessageVM getConfigurationIntermediateValues(@RequestParam String plantId,@RequestParam String year) {
		return crackerReportService.getConfigurationIntermediateValues(plantId,year);
	}
	
	@GetMapping(value="/finding-model")
	public AOPMessageVM getFindingModel(@RequestParam String plantId,@RequestParam String year) {
		return crackerReportService.getFindingModel(plantId,year);
	}
	
	@GetMapping(value="/miis-data")
	public AOPMessageVM getMIISData(@RequestParam String plantId,@RequestParam String year) {
		return crackerReportService.getMIISData(plantId,year);
	}


}
