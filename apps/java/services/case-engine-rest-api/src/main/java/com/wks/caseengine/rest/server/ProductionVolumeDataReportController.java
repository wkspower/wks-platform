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

import com.wks.caseengine.dto.MonthWiseConsumptionSummaryDTO;
import com.wks.caseengine.dto.MonthWiseProductionPlanDTO;
import com.wks.caseengine.dto.PlantProductionDTO;
import com.wks.caseengine.dto.PlantProductionDataDTO;
import com.wks.caseengine.dto.TurnAroundPlanReportDTO;
import com.wks.caseengine.entity.PlantProductionRequestDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.ProductionVolumeDataReportService;

@RestController
@RequestMapping("task")
public class ProductionVolumeDataReportController {

	@Autowired
	private ProductionVolumeDataReportService productionVolumeDataReportService;

	@GetMapping(value = "/report/production-summary")
	public ResponseEntity<AOPMessageVM> getReportForProductionVolumnData(@RequestParam String plantId,
			@RequestParam String year) {
		AOPMessageVM response = productionVolumeDataReportService.getReportForProductionVolumnData(plantId, year);
		return ResponseEntity.status(response.getCode()).body(response);
	}

	@GetMapping(value = "/report/month-wise/production")
	public ResponseEntity<AOPMessageVM> getReportForMonthWiseProductionData(@RequestParam String plantId,
			@RequestParam String year) {
		AOPMessageVM response = productionVolumeDataReportService.getReportForMonthWiseProductionData(plantId, year);
		return ResponseEntity.status(response.getCode()).body(response);
	}

	@GetMapping(value = "/report/month-wise/consumption-summary")
	public ResponseEntity<AOPMessageVM> getReportForMonthWiseConsumptionSummaryData(@RequestParam String plantId,
			@RequestParam String year,@RequestParam String reportType) {
		AOPMessageVM response = productionVolumeDataReportService.getReportForMonthWiseConsumptionSummaryData(plantId,
				year,reportType);
		return ResponseEntity.status(response.getCode()).body(response);
	}
	
	@PostMapping(value = "/report/month-wise/consumption-summary")
	public ResponseEntity<AOPMessageVM> updateReportForMonthWiseConsumptionSummaryData(@RequestParam String plantId,
			@RequestParam String year,@RequestBody List<MonthWiseConsumptionSummaryDTO> dataList) {
		AOPMessageVM response = productionVolumeDataReportService.updateReportForMonthWiseConsumptionSummaryData(plantId, year, dataList);
		return ResponseEntity.status(response.getCode()).body(response);
	}

	@GetMapping(value = "/report/plant/production/plan")
	public ResponseEntity<AOPMessageVM> getReportForPlantProductionPlanData(@RequestParam String plantId,
			@RequestParam String year, @RequestParam String reportType) {
		AOPMessageVM response = productionVolumeDataReportService.getReportForPlantProductionPlanData(plantId, year,
				reportType);
		return ResponseEntity.status(response.getCode()).body(response);
	}
	
	@PostMapping(value = "/report/plant/production/plan")
	public ResponseEntity<AOPMessageVM> updateReportForPlantProductionPlanData(@RequestParam String plantId,
			@RequestParam String year,
			@RequestBody List<PlantProductionDTO> dataList) {
		AOPMessageVM response = productionVolumeDataReportService.updateReportForPlantProductionPlanData(plantId, year, dataList);
		return ResponseEntity.status(response.getCode()).body(response);
	}

	@GetMapping(value = "/report/plant/contribution/year-wise/plan")
	public ResponseEntity<AOPMessageVM> getReportForPlantContributionYearWise(@RequestParam String plantId,
			@RequestParam String year, @RequestParam String reportType) {
		AOPMessageVM response = productionVolumeDataReportService.getReportForPlantContributionYearWise(plantId, year,
				reportType);
		return ResponseEntity.status(response.getCode()).body(response);
	}

	// MAPPING for saving plant production remarks
	@PostMapping("/plant-production-data")
	public ResponseEntity<AOPMessageVM> savePlantProductionData(
			@RequestParam String plantId,
			@RequestParam String year,
			@RequestBody List<PlantProductionDataDTO> dataList) {
		AOPMessageVM response = productionVolumeDataReportService.savePlantProductionData(plantId, year, dataList);
		return ResponseEntity.status(response.getCode()).body(response);


	}
	@GetMapping("/handle/calculate/plant-production-summary")
	public ResponseEntity<AOPMessageVM> calculateProductionSummary(
			@RequestParam String plantId,
			@RequestParam String year) {
		AOPMessageVM response =  productionVolumeDataReportService.calculateProductionSummary(year,plantId);
		return ResponseEntity.status(response.getCode()).body(response);


	}
	@GetMapping("/handle/calculate/monthwise-production-data")
	public ResponseEntity<AOPMessageVM> calculateMonthwiseProductionData(
			@RequestParam String plantId,
			@RequestParam String year) {
		AOPMessageVM response =  productionVolumeDataReportService.calculateMonthwiseProductionData(year,plantId);
		return ResponseEntity.status(response.getCode()).body(response);


	}

	    
    // AOPMessageVM calculatePlantConsumptionSummaryReportData(String year, String plantId);
    // AOPMessageVM calculateTurnAroundPlanReportData(String year, String plantId);
    // AOPMessageVM calculateAnnualProductionPlanData(String year, String plantId);
    // AOPMessageVM calculatePlantContributionReportData(String year, String plantId);

		@GetMapping("/handle/calculate/plant-consumption-data")
	public ResponseEntity<AOPMessageVM> calculatePlantConsumptionSummaryReportData(
			@RequestParam String plantId,
			@RequestParam String year) {
		AOPMessageVM response =  productionVolumeDataReportService.calculatePlantConsumptionSummaryReportData(year,plantId);
		return ResponseEntity.status(response.getCode()).body(response);


	}
		@GetMapping("/handle/calculate/turnarounf-plan-data")
	public ResponseEntity<AOPMessageVM> calculateTurnAroundPlanReportData(
			@RequestParam String plantId,
			@RequestParam String year) {
		AOPMessageVM response =  productionVolumeDataReportService.calculateTurnAroundPlanReportData(year,plantId);
		return ResponseEntity.status(response.getCode()).body(response);


	}
		@GetMapping("/handle/calculate/annual-production-data")
	public ResponseEntity<AOPMessageVM> calculateAnnualProductionPlanData(
			@RequestParam String plantId,
			@RequestParam String year) {
		AOPMessageVM response =  productionVolumeDataReportService.calculateAnnualProductionPlanData(year,plantId);
		return ResponseEntity.status(response.getCode()).body(response);


	}
		@GetMapping("/handle/calculate/plan-contribution-data")
	public ResponseEntity<AOPMessageVM> calculatePlantContributionReportData(
			@RequestParam String plantId,
			@RequestParam String year) {
		AOPMessageVM response =  productionVolumeDataReportService.calculatePlantContributionReportData(year,plantId);
		return ResponseEntity.status(response.getCode()).body(response);


	}

	// ==================

	@PostMapping("/monthwise-production-data")
	public ResponseEntity<AOPMessageVM> saveMonthWiseProductionPlanData(
			@RequestParam String plantId,
			@RequestParam String year,
			@RequestBody List<MonthWiseProductionPlanDTO> dataList) {
		AOPMessageVM response = productionVolumeDataReportService.saveMonthWiseProductionPlanData(plantId, year, dataList);
		return ResponseEntity.status(response.getCode()).body(response);


	}
	@PostMapping("/turnaround-data")
	public ResponseEntity<AOPMessageVM> savePlanTurnAroundData(
			@RequestParam String plantId,
			@RequestParam String year,
			@RequestBody List<TurnAroundPlanReportDTO> dataList) {
		AOPMessageVM response = productionVolumeDataReportService.savePlanTurnAroundData(plantId, year, dataList);
		return ResponseEntity.status(response.getCode()).body(response);


	}

	

}
