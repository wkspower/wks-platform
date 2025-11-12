package com.wks.caseengine.rest.server;


import java.util.List;

import java.util.UUID;

import com.wks.caseengine.service.SpyroOutputService;
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


import com.wks.caseengine.dto.SpyroOutputDTO;
import com.wks.caseengine.dto.YieldDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

@RestController
@RequestMapping("task")
public class SpyroOutputController {
	
	@Autowired
	private SpyroOutputService spyroOutputService;
	
	@GetMapping(value="/spyro-output")
	public AOPMessageVM getSpyroOutputData(@RequestParam String year,@RequestParam String plantId,@RequestParam String Mode,@RequestParam(value = "type", required = false) String type){
		return	spyroOutputService.getSpyroOutputData(year, plantId,Mode,type);
	}

	@PostMapping(value="/spyro-output")
	public AOPMessageVM updateSpyroOutputData(@RequestParam String year,@RequestParam String plantId,@RequestBody List<SpyroOutputDTO> spyroOutputDTOList){
		return spyroOutputService.updateSpyroOutputData(year,plantId,spyroOutputDTOList);
	}
	
	@GetMapping(value="/spyro-output/yield")
	public AOPMessageVM getSpyroOutputYieldData(@RequestParam String year,@RequestParam String plantId){
		return	spyroOutputService.getSpyroOutputYieldData(year, plantId);
	}
	
	@GetMapping(value = "/yield-export")
	public ResponseEntity<byte[]> exportYieldReport(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year
	        ) {
	    try {
			
	        byte[] excelBytes = spyroOutputService.exportYieldReport(year,plantId,false,null); //excelService.generateFlexibleExcel(data, plantId, year);//productionVolumeDataReportExportService.getReportForPlantProductionPlanData(plantId, year, reportType);

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("Yield_Report.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	@PostMapping(value="/spyro-output/yield")
	public AOPMessageVM updateSpyroOutputYieldData(
	    @RequestParam String plantId,
	    @RequestParam String year,
	    @RequestBody List<YieldDTO> payload
	) {
	    
	    return spyroOutputService.updateSpyroOutputYieldData(plantId, year, payload);
	}
	
	@GetMapping(value = "/spyro-output-export-excel")
	public ResponseEntity<byte[]> exportSpyroOutputReport(
	         @RequestParam String year,@RequestParam String plantId,@RequestParam String mode,@RequestParam(value = "type", required = false) String type
	        ) {
	    try {
			
	        byte[] excelBytes = spyroOutputService.createExcel(year, plantId, mode, false, null);

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("SpyroOutput.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	@PostMapping(value = "/yield-import", consumes = "multipart/form-data")
	public AOPMessageVM importYieldExcel(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,
			@RequestParam("file") MultipartFile file
	        ) {
			return	spyroOutputService.importYieldExcel(year,UUID.fromString(plantId), file); 
	}
	
	@PostMapping(value = "/spyro-output-import-excel", consumes = "multipart/form-data")
	public AOPMessageVM importExcel(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,
			@RequestParam("mode") String mode,
			@RequestParam("file") MultipartFile file
	        ) {
			return	spyroOutputService.importExcel(year, plantId, mode, file); 
	}


}
