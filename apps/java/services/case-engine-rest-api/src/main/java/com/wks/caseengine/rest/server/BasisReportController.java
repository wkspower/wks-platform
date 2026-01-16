package com.wks.caseengine.rest.server;



import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.BasisReportService;

@RestController
@RequestMapping("task")
public class BasisReportController {
	
	@Autowired
	private BasisReportService basisReportService;
	
	@GetMapping(value="/data-set-norms-historian")
	public ResponseEntity<AOPMessageVM> getNormhistorian(@RequestParam String plantId,@RequestParam String year,@RequestParam(value="periodFrom", required=false) String periodFrom,@RequestParam(value="periodTo", required=false) String periodTo,@RequestParam(value="type", required=false) String type){
		AOPMessageVM response=basisReportService.getNormhistorian(plantId,year,periodFrom,periodTo,type);
		return ResponseEntity.status(response.getCode()).body(response);
	}
	
	@GetMapping(value = "/data-set-norms-historian-export")
	public ResponseEntity<byte[]> exportNormhistorian(
			@RequestParam String plantId,@RequestParam String year,@RequestParam(value="periodFrom", required=false) String periodFrom,@RequestParam(value="periodTo", required=false) String periodTo,@RequestParam(value="type", required=false) String type
	        ) {
	    try {
			
	        byte[] excelBytes = basisReportService.exportNormhistorian(plantId,year,periodFrom,periodTo,type); //excelService.generateFlexibleExcel(data, plantId, year);//productionVolumeDataReportExportService.getReportForPlantProductionPlanData(plantId, year, reportType);

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("Norm_Historian.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	@GetMapping(value="/data-set-production-target")
	public ResponseEntity<AOPMessageVM> getProductionTarget(@RequestParam String plantId,@RequestParam String year){
		AOPMessageVM response=basisReportService.getProductionTarget(plantId,year);
		return ResponseEntity.status(response.getCode()).body(response);
	}
	
	@GetMapping(value="/report/norms-basis/mode")
	public AOPMessageVM getNormBasisReportCracker(@RequestParam String plantId,@RequestParam String year,@RequestParam(value="type", required=false) String type,@RequestParam(value="mode", required=false) String mode){
		return basisReportService.getNormBasisReportCracker(plantId,year,type,mode);
	}
	
	@GetMapping(value="/report/best-achieved")
	public AOPMessageVM getBestAchievedCracker(@RequestParam String plantId,@RequestParam String year,@RequestParam(value="reportType", required=false) String reportType){
		return basisReportService.getBestAchievedCracker(plantId,year,reportType);
	}
	
	@GetMapping(value = "/best-achieved-export")
	public ResponseEntity<byte[]> exportBestAchieved(
			@RequestParam String plantId,@RequestParam String year,@RequestParam(value="reportType", required=false) String reportType
	        ) {
	    try {
			
	        byte[] excelBytes = basisReportService.exportBestAchieved(plantId,year,reportType); //excelService.generateFlexibleExcel(data, plantId, year);//productionVolumeDataReportExportService.getReportForPlantProductionPlanData(plantId, year, reportType);

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("Best_Achieved.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}

		
	@GetMapping(value="/calculate-best-achieved")
	public AOPMessageVM calculateBestAchieved(@RequestParam String year,@RequestParam String plantId,@RequestParam(required=false) String periodTo,@RequestParam(required=false) String periodFrom){
		return	basisReportService.calculateBestAchieved(year, plantId,periodTo,periodFrom);
	}

	@GetMapping(value="/calculate-best-achieved-individual")
	public AOPMessageVM calculateBestAchievedIndividual(@RequestParam String year,@RequestParam String plantId,@RequestParam(required=false) String periodTo,@RequestParam(required=false) String periodFrom){
		return	basisReportService.calculateBestAchievedIndividual(year, plantId,periodTo,periodFrom);
	}
	
	
	
	
}

