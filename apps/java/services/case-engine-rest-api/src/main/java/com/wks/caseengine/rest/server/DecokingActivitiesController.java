package com.wks.caseengine.rest.server;

import java.util.List;
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

import com.wks.caseengine.dto.DecokePlanningDTO;
import com.wks.caseengine.dto.DecokePlanningIBRDTO;
import com.wks.caseengine.dto.DecokeRunLengthDTO;
import com.wks.caseengine.dto.DecokingActivitiesDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.DecokingActivitiesService;

@RestController
@RequestMapping("task")
public class DecokingActivitiesController {
	
	@Autowired
	private DecokingActivitiesService decokingActivitiesService;
	
	@GetMapping(value="/decoking-activities")
	public AOPMessageVM getDecokingActivitiesData(@RequestParam(value = "year", required = false) String year,@RequestParam String plantId,@RequestParam(value = "reportType", required = false) String reportType) {
		return decokingActivitiesService.getDecokingActivitiesData(year,plantId,reportType);
	}
	
	@GetMapping(value="/decoking-activities/ibr")
	public AOPMessageVM getDecokingActivitiesIBRData(@RequestParam(value = "year", required = false) String year,@RequestParam String plantId,@RequestParam(value = "reportType", required = false) String reportType) {
		return decokingActivitiesService.getDecokingActivitiesIBRData(year,plantId,reportType);
	}
	
	@PostMapping(value="/decoking-activities")
	public AOPMessageVM updateDecokingActivitiesData(@RequestParam(value = "year", required = false) String year,@RequestParam String plantId,@RequestParam(value = "reportType", required = false) String reportType, @RequestBody List<DecokingActivitiesDTO> decokingActivitiesDTOList) {
		return decokingActivitiesService.updateDecokingActivitiesData(year,plantId,reportType,decokingActivitiesDTOList);
	}
	
	@PostMapping(value="/decoking-activities/ibr")
	public AOPMessageVM updateDecokingActivitiesIBRData(@RequestParam(value = "year", required = false) String year,@RequestParam String plantId,@RequestParam(value = "reportType", required = false) String reportType, @RequestBody List<DecokePlanningIBRDTO> decokePlanningIBRDTOList) {
		return decokingActivitiesService.updateDecokingActivitiesIBRData(year,plantId,reportType,decokePlanningIBRDTOList);
	}


	
	@GetMapping(value = "/run-length-export-excel")
	public ResponseEntity<byte[]> exportConfigurationReport(
	         @RequestParam(value = "year", required = false) String year,@RequestParam String plantId,@RequestParam(value = "reportType", required = false) String reportType
	        ) {
	    try {
			byte[] excelBytes = decokingActivitiesService.createExcel(year,plantId,reportType,false, null);
	       // byte[] excelBytes = configurationService.createExcel(year,UUID.fromString(plantId), false,null); //excelService.generateFlexibleExcel(data, plantId, year);//productionVolumeDataReportExportService.getReportForPlantProductionPlanData(plantId, year, reportType);

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("decocking-activities.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}


	@PostMapping(value = "/run-length-import-excel", consumes = "multipart/form-data")
	public AOPMessageVM importExcel(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,
			@RequestParam("reportType") String reportType,
			@RequestParam("file") MultipartFile file
	        ) {
			return	decokingActivitiesService.importExcel(year,UUID.fromString(plantId),reportType, file); 
	}
	
	@PostMapping(value="/decoking-activities/run-length")
	public AOPMessageVM updateDecokingActivitiesRunLengthData(@RequestParam(value = "year", required = false) String year,@RequestParam String plantId,@RequestParam(value = "reportType", required = false) String reportType, @RequestBody List<DecokeRunLengthDTO> decokeRunLengthDTOList) {
		return decokingActivitiesService.updateDecokingActivitiesRunLengthData(year,plantId,reportType,decokeRunLengthDTOList);
	}
	@GetMapping(value="/calculate/decoking-activities")
	public AOPMessageVM calculateDecokingActivities(@RequestParam(value = "year", required = false) String year,@RequestParam String plantId,@RequestParam(value = "reportType", required = false) String reportType) {
		return decokingActivitiesService.calculateDecokingActivities(plantId,year);
	}
}
