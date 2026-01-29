package com.wks.caseengine.rest.server;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.BudgetMaintenanceDto;
import com.wks.caseengine.dto.DecokePlanningDTO;
import com.wks.caseengine.dto.MaintenanceDetailsDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.MaintenanceCalculatedDataNMDService;
import com.wks.caseengine.service.MaintenanceCalculatedDataService;

@RestController
@RequestMapping("task")
public class MaintenanceCalculatedDataController {
	
	@Autowired
	private MaintenanceCalculatedDataService maintenanceCalculatedDataService;
	
	@Autowired
	private MaintenanceCalculatedDataNMDService maintenanceCalculatedDataNMDService;
	
	@GetMapping(value="/maintenance-details")
	public List<MaintenanceDetailsDTO> getMaintenanceCalculatedData(@RequestParam String plantId, @RequestParam String year){
		return maintenanceCalculatedDataService.getMaintenanceCalculatedData(plantId,year);		
	}
	
	@GetMapping(value="/maintenance-report-urls")
	public AOPMessageVM getMaintenanceReportURLs(@RequestParam String plantId, @RequestParam String year, @RequestParam String type){
		return maintenanceCalculatedDataService.getMaintenanceReportURLs(plantId,year,type);		
	}
	
	@GetMapping(value="/maintenance")
	public AOPMessageVM getMaintenanceDataForCracker(@RequestParam String plantId, @RequestParam String year){
		return maintenanceCalculatedDataService.getMaintenanceDataForCracker(plantId,year);		
	}
	
	@GetMapping(value="/maintenance-nmd")
	public AOPMessageVM getMaintenanceDataNMDForCracker(@RequestParam String plantId, @RequestParam String year){
		return maintenanceCalculatedDataNMDService.getMaintenanceDataForCracker(plantId,year);		
	}
	
	@GetMapping(value = "/maintenance-export")
	public ResponseEntity<byte[]> maintenanceExport(
	         @RequestParam String year,@RequestParam String plantId) {
	    try {
			
	        byte[] excelBytes = maintenanceCalculatedDataService.maintenanceExport(year, plantId, false, null);

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("maintenance.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}

	
	@PostMapping(value="/maintenance")
	public AOPMessageVM updateMaintenanceDataForCracker(@RequestParam String plantId, @RequestParam String year, @RequestBody List<Map<String, Object>> payloadList){
		return maintenanceCalculatedDataService.updateMaintenanceDataForCracker(plantId,year,payloadList);		
	}
	@PostMapping(value="/maintenance-nmd")
	public AOPMessageVM updateMaintenanceDataNMDForCracker(@RequestParam String plantId, @RequestParam String year,@RequestBody List<DecokePlanningDTO> decokePlanningDTOList){
		return maintenanceCalculatedDataNMDService.updateMaintenanceDataForCracker(plantId,year,decokePlanningDTOList);		
	}
	
	@PostMapping(value = "/maintenance-import", consumes = "multipart/form-data")
	public AOPMessageVM maintenanceImport(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,
			@RequestParam("file") MultipartFile file
	        ) {
			return	maintenanceCalculatedDataService.maintenanceImport(year,UUID.fromString(plantId), file); 
	}
	
	@GetMapping(value="/budget-maintenance")
	public AOPMessageVM getBudgetMaintenance(@RequestParam String plantId, @RequestParam String year,@RequestParam(required=false) String budgetCategory){
		return maintenanceCalculatedDataService.getBudgetMaintenance(plantId,year,budgetCategory);		
	}
	
	@PostMapping(value="/budget-maintenance")
	public AOPMessageVM updateBudgetMaintenance(@RequestBody List<BudgetMaintenanceDto> budgetMaintenanceDtos){
		return maintenanceCalculatedDataService.updateBudgetMaintenance(budgetMaintenanceDtos);		
	}
	
	@GetMapping(value = "/budget-maintenance-export-excel")
	public ResponseEntity<byte[]> exportBudgetMaintenance(
	         @RequestParam(value = "year", required = false) String year,@RequestParam String plantId,@RequestParam(required = false) String budgetCategory) {
	    try {
			byte[] excelBytes = maintenanceCalculatedDataService.createExcel(year,plantId,false, null);
	       // byte[] excelBytes = configurationService.createExcel(year,UUID.fromString(plantId), false,null); //excelService.generateFlexibleExcel(data, plantId, year);//productionVolumeDataReportExportService.getReportForPlantProductionPlanData(plantId, year, reportType);

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("budget-maintenance.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	@PostMapping(value = "/budget-maintenance-import-excel", consumes = "multipart/form-data")
	public AOPMessageVM importExcel(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,
			@RequestParam(value = "budgetCategory", required = false) String budgetCategory,
			@RequestParam("file") MultipartFile file
	        ) {
			return	maintenanceCalculatedDataService.importExcel(year, plantId, budgetCategory, file); 
	}

	
	@GetMapping(value="/macro")
	public AOPMessageVM getMacroData(@RequestParam Double value, @RequestParam String year,@RequestParam String plantId){
		return maintenanceCalculatedDataService.getMacroData(value,year,plantId);		
	}
}
