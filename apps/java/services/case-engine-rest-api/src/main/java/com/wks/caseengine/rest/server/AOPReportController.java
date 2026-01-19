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

import com.wks.caseengine.dto.PlantContributionSummaryDTO;
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
	
	@GetMapping(value="/report/production-volume-aop")
	public ResponseEntity<AOPMessageVM> getReportForProductionVolumnData(@RequestParam String plantId,@RequestParam String year,@RequestParam String reportType,@RequestParam(value = "uom", required = false) String uom){
		AOPMessageVM response	=aopReportService.getReportForProductionVolumnData(plantId,year,reportType,uom);
		return ResponseEntity.status(response.getCode()).body(response);
	}
	
	@GetMapping(value="/handle/calculate/miis-contribution")
	public ResponseEntity<AOPMessageVM> getHandleCalculateMIISContribution(@RequestParam String plantId,@RequestParam String year){
		AOPMessageVM response	=aopReportService.getHandleCalculateMIISContribution(plantId,year);
		return ResponseEntity.status(response.getCode()).body(response);
	}
	
	@GetMapping(value="/plant-contribution-years-wise")
	public AOPMessageVM getFiveYearSummaryReport(@RequestParam String plantId,@RequestParam String year,@RequestParam(required=false) String reportType){
		return aopReportService.getFiveYearSummaryReport(plantId,year,reportType);
	}
	
	@GetMapping(value="/report-plant-contribution-summary-yearly")
	public AOPMessageVM getPlantContributionFiveYearSummaryReport(@RequestParam(required=false) String reportType,@RequestParam String plantId,@RequestParam String year) {
		return aopReportService.getPlantContributionFiveYearSummaryReport(reportType,plantId,year);
	}
	
	@GetMapping(value="/specific-consumption-norms")
	public AOPMessageVM getSpecificConsumptionNormsReport(@RequestParam(required=false) String reportType,@RequestParam String plantId,@RequestParam String year) {
		return aopReportService.getSpecificConsumptionNormsReport(reportType,plantId,year);
	}
	
	@GetMapping(value="/specific-consumption-t17")
	public AOPMessageVM getSpecificConsumptionNormsT17Report(@RequestParam(required=false) String reportType,@RequestParam String plantId,@RequestParam String year) {
		return aopReportService.getSpecificConsumptionNormsT17Report(reportType,plantId,year);
	}
	
	@PostMapping(value="/report-plant-contribution-summary-yearly")
	public AOPMessageVM updatePlantContributionFiveYearSummaryReport(@RequestBody List<PlantContributionSummaryDTO> plantContributionSummaryDTOs) {
		return aopReportService.updatePlantContributionFiveYearSummaryReport(plantContributionSummaryDTOs);
	}
	
	@GetMapping(value="/grade-wise-consumption-norms")
	public AOPMessageVM getGradewiseConsumptionNorms(@RequestParam String plantId,@RequestParam String year,@RequestParam(required=false) String reportType) {
		return aopReportService.getGradewiseConsumptionNorms(plantId,year,reportType);
	}

}
