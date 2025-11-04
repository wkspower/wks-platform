package com.wks.caseengine.rest.server;



import java.util.List;

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

import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.ReliabilityService;
import com.wks.caseengine.dto.ReliabilityPerformanceDto;
import com.wks.caseengine.dto.ReliabilityRecordDto;

@RestController
@RequestMapping("task")
public class ReliabilityController {
	
	@Autowired
	private ReliabilityService reliabilityService;
	
	@GetMapping(value="/reliability-performance")
	public AOPMessageVM getReliabilityPerformance(@RequestParam String plantId,@RequestParam String year,@RequestParam(required=false) String type){
		 return  reliabilityService.getReliabilityPerformance(plantId,year,type);
	}
	
	@GetMapping(value = "/reliability-performance-export-excel")
	public ResponseEntity<byte[]> exportReliabilityPerformance(
	         @RequestParam(value = "year", required = false) String year,@RequestParam String plantId) {
	    try {
			byte[] excelBytes = reliabilityService.createExcel(year,plantId,false, null);
	       // byte[] excelBytes = configurationService.createExcel(year,UUID.fromString(plantId), false,null); //excelService.generateFlexibleExcel(data, plantId, year);//productionVolumeDataReportExportService.getReportForPlantProductionPlanData(plantId, year, reportType);

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("reliability-performance.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	@PostMapping(value="/reliability-performance")
	public AOPMessageVM updateReliabilityPerformance(@RequestBody List<ReliabilityPerformanceDto> reliabilityPerformanceDtos){
		 return  reliabilityService.updateReliabilityPerformance(reliabilityPerformanceDtos);
	}
	
	@PostMapping(value = "/reliability-performance-import-excel", consumes = "multipart/form-data")
	public AOPMessageVM importExcel(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,
			@RequestParam("file") MultipartFile file
	        ) {
			return	reliabilityService.importExcel(year, plantId, file); 
	}

	
	@GetMapping(value="/reliability-records")
	public AOPMessageVM getReliabilityRecords(@RequestParam String plantId,@RequestParam String year,@RequestParam(required=false) String type){
		 return  reliabilityService.getReliabilityRecords(plantId,year,type);
	}
	
	@GetMapping(value = "/reliability-records-export-excel")
	public ResponseEntity<byte[]> exportReliabilityRecords(
	         @RequestParam(value = "year", required = false) String year,@RequestParam String plantId) {
	    try {
			byte[] excelBytes = reliabilityService.exportReliabilityRecords(year,plantId,false, null);
	       // byte[] excelBytes = configurationService.createExcel(year,UUID.fromString(plantId), false,null); //excelService.generateFlexibleExcel(data, plantId, year);//productionVolumeDataReportExportService.getReportForPlantProductionPlanData(plantId, year, reportType);

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("reliability-records.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	@PostMapping(value = "/reliability-records-import-excel", consumes = "multipart/form-data")
	public AOPMessageVM importReliabilityRecords(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,
			@RequestParam("file") MultipartFile file
	        ) {
			return	reliabilityService.importReliabilityRecords(year, plantId, file); 
	}
	
	@PostMapping(value="/reliability-records")
	public AOPMessageVM updateReliabilityRecords(@RequestBody List<ReliabilityRecordDto> reliabilityRecordDtos){
		 return  reliabilityService.updateReliabilityRecords(reliabilityRecordDtos);
	}
	
	
  
}
