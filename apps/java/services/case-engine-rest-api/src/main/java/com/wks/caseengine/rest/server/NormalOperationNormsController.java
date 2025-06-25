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

import com.wks.caseengine.dto.MCUNormsValueDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.NormalOperationNormsService;

@RestController
@RequestMapping("task")
public class NormalOperationNormsController {
	
	@Autowired
	private NormalOperationNormsService normalOperationNormsService;
	
	@GetMapping(value="/normalOperationNorms")
	public AOPMessageVM getNormalOperationNormsData(@RequestParam String year,@RequestParam String plantId){
		return	normalOperationNormsService.getNormalOperationNormsData(year, plantId);
	}
	
	@GetMapping(value="/calculate-normal-ops-norms")
	public AOPMessageVM calculateNormalOpsNorms(@RequestParam String aopYear,@RequestParam String plantId,@RequestParam String siteId,@RequestParam String verticalId){
		return	normalOperationNormsService.calculateNormalOpsNorms(aopYear, plantId,siteId,verticalId);
	}

	@GetMapping(value = "/norms-transactions")
	public AOPMessageVM getNormsTransaction(@RequestParam String plantId, @RequestParam String year) {
		return normalOperationNormsService.getNormsTransaction(plantId, year);
	}

	@PostMapping(value = "/normalOperationNorms")
	public List<MCUNormsValueDTO> saveNormalOperationNormsData(
		@RequestParam String plantId, @RequestParam String year,
			@RequestBody List<MCUNormsValueDTO> mCUNormsValueDTOList) {
		try {
			return normalOperationNormsService.saveNormalOperationNormsData(mCUNormsValueDTOList,UUID.fromString(plantId),year);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@GetMapping(value = "/handleCalculateNormalOpsNorms")
	public AOPMessageVM getNormalOperationNormsDataFromSP(@RequestParam String year, @RequestParam String plantId) {
		return normalOperationNormsService.calculateExpressionConsumptionNorms(year, plantId);
	}
	
	
	@GetMapping(value = "/norms-export-excel")
	public ResponseEntity<byte[]> exportPlantProductionPlanReport(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year
	        ) {
	    try {
			
	        byte[] excelBytes = normalOperationNormsService.createExcel(year,UUID.fromString(plantId),false,null); //excelService.generateFlexibleExcel(data, plantId, year);//productionVolumeDataReportExportService.getReportForPlantProductionPlanData(plantId, year, reportType);

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("plant_production_plan.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}



	@PostMapping(value = "/norms-import-excel", consumes = "multipart/form-data")
	public ResponseEntity<byte[]> importExcel(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,
			@RequestParam("file") MultipartFile file
	        ) {
			byte[] excelBytes =	 normalOperationNormsService.importExcel(year,UUID.fromString(plantId), file); 
	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("Normal Op Norms.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	}
	
}
