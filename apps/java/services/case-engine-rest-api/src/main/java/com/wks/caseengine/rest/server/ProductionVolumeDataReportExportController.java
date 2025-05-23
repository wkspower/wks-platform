package com.wks.caseengine.rest.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.ProductionVolumeDataReportExportService;

@RestController
@RequestMapping("task")
public class ProductionVolumeDataReportExportController {
	
	@Autowired
	private ProductionVolumeDataReportExportService productionVolumeDataReportExportService;
	
	@GetMapping(value = "/export/report/plant/production/plan")
	public ResponseEntity<byte[]> exportPlantProductionPlanReport(
	        @RequestParam String plantId,
	        @RequestParam String year,
	        @RequestParam String reportType) {
	    try {
	        byte[] excelBytes = productionVolumeDataReportExportService.getReportForPlantProductionPlanData(plantId, year, reportType);

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


}
