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
	public ResponseEntity<AOPMessageVM> getReportForProductionVolumnData(@RequestParam String plantId,@RequestParam String year){
		AOPMessageVM response	=productionVolumeDataReportService.getReportForProductionVolumnData(plantId,year);
		return ResponseEntity.status(response.getCode()).body(response);
	}
	
	@GetMapping(value="/report/month-wise/production")
	public ResponseEntity<AOPMessageVM> getReportForMonthWiseProductionData(@RequestParam String plantId,@RequestParam String year){
		AOPMessageVM response	=productionVolumeDataReportService.getReportForMonthWiseProductionData(plantId,year);
		return ResponseEntity.status(response.getCode()).body(response);
	}

	@GetMapping(value="/report/month-wise/consumption-summary")
	public ResponseEntity<AOPMessageVM> getReportForMonthWiseConsumptionSummaryData(@RequestParam String plantId,@RequestParam String year){
		AOPMessageVM response	=productionVolumeDataReportService.getReportForMonthWiseConsumptionSummaryData(plantId,year);
		return ResponseEntity.status(response.getCode()).body(response);
	}
	
	@GetMapping(value="/report/plant/production/plan")
	public ResponseEntity<AOPMessageVM> getReportForPlantProductionPlanData(@RequestParam String plantId,@RequestParam String year,@RequestParam String reportType){
		AOPMessageVM response	=productionVolumeDataReportService.getReportForPlantProductionPlanData(plantId,year,reportType);
		return ResponseEntity.status(response.getCode()).body(response);
	}


}
