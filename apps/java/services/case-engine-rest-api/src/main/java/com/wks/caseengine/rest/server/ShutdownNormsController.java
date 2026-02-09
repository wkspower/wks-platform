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

import com.wks.caseengine.dto.ShutdownNormsValueDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.ShutdownNormsService;

@RestController
@RequestMapping("task")
public class ShutdownNormsController {
	
	@Autowired
	private ShutdownNormsService shutdownNormsService;
	
	@GetMapping(value = "/shutdown-consumption")
	public AOPMessageVM getShutdownNormsData(@RequestParam String year, @RequestParam String plantId,
			@RequestParam(required = false) String gradeId) {
		return shutdownNormsService.getShutdownNormsData(year, plantId, gradeId);
	}
	
	@GetMapping(value = "/export-shutdown-consumption")
	public ResponseEntity<byte[]> exportShutdownConsumption(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year, @RequestParam(required = false) String gradeId
	        ) {
	    try {
			
	        byte[] excelBytes = shutdownNormsService.exportShutdownConsumption(year,UUID.fromString(plantId),false,null,gradeId); //excelService.generateFlexibleExcel(data, plantId, year);//productionVolumeDataReportExportService.getReportForPlantProductionPlanData(plantId, year, reportType);

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("Shutdown_Consumption.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	@PostMapping(value = "/import-shutdown-consumption", consumes = "multipart/form-data")
	public AOPMessageVM importExcel(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,
            @RequestParam(required = false) String gradeId,
			@RequestParam("file") MultipartFile file,@RequestParam(required = false) String mode
	        ) {
			return	shutdownNormsService.importShutdownConsumption(year,UUID.fromString(plantId),gradeId, file); 
	}

	@GetMapping(value = "/shutdown-consumption-export")
	public ResponseEntity<byte[]> exportShutdownNorms(
			@RequestParam("plantId") String plantId,
			@RequestParam("year") String year,
			@RequestParam(value = "allGrade", required = false, defaultValue = "false") boolean allGrade

	) {
		try {

			byte[] excelBytes = shutdownNormsService.exportShutdownNorms(
					year,
					UUID.fromString(plantId),
					false,
					null,
					allGrade);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(
					MediaType.parseMediaType(
							"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
			headers.setContentDisposition(
					ContentDisposition.builder("attachment")
							.filename("ShutdownNorms.xlsx")
							.build());
			headers.setContentLength(excelBytes.length);

			return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);

		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(value = "/shutdown-consumption-import", consumes = "multipart/form-data")
	public AOPMessageVM importExcel(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,
            @RequestParam(required = false) String gradeId,
			@RequestParam("file") MultipartFile file,@RequestParam(required = false) boolean allGrade
	        ) {
			return	shutdownNormsService.importExcel(year,UUID.fromString(plantId),gradeId, file,allGrade); 
	}

	@GetMapping(value="/shutdown-consumption-history-data")
	public AOPMessageVM getShutConsumptionData(@RequestParam String year,@RequestParam String plantId,@RequestParam(required=false) String gradeId){
		return	shutdownNormsService.getShutConsumptionData(year,plantId,gradeId);
	}
	
	@PostMapping(value="/shutdown-consumption")
	public AOPMessageVM saveShutdownNormsData(@RequestParam String plantId,@RequestBody List<ShutdownNormsValueDTO> shutdownNormsValueDTOList){
		return	shutdownNormsService.saveShutDownNorms(plantId,shutdownNormsValueDTOList);
	}
	
	@GetMapping(value="/calculate-shutdown-consumption")
	public AOPMessageVM getShutdownNormsSPData(@RequestParam String year,@RequestParam String plantId){
		return	shutdownNormsService.getShutdownNormsSPData(year,plantId);
	}
	
	@GetMapping(value="/unique/grades")
	public AOPMessageVM getUniqueGrades(@RequestParam String year,@RequestParam String plantId){
		return	shutdownNormsService.getUniqueGrades(year,plantId);
	}

}

