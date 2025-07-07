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

import com.wks.caseengine.dto.ConfigurationDTO;
import com.wks.caseengine.dto.ExecutionDetailDto;
import com.wks.caseengine.dto.NormAttributeTransactionReceipeDTO;
import com.wks.caseengine.dto.NormAttributeTransactionReceipeRequestDTO;
import com.wks.caseengine.entity.NormAttributeTransactionReceipe;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.ConfigurationService;

@RestController
@RequestMapping("task")
public class ConfigurationController {
	
	@Autowired
	private ConfigurationService configurationService;
	
	@GetMapping(value="/getConfigurationData")
	public List<ConfigurationDTO> getConfigurationData(@RequestParam String year,@RequestParam UUID plantFKId) {
		return configurationService.getConfigurationData(year,plantFKId);
	}
	
	@GetMapping(value="/configuration/intermediate-values")
	public AOPMessageVM getConfigurationIntermediateValues(@RequestParam String year,@RequestParam UUID plantFKId) {
		return configurationService.getConfigurationIntermediateValues(year,plantFKId);
	}
	
	@GetMapping(value="/get/configuration/intermediate-values")
	public AOPMessageVM getConfigurationIntermediateValuesData(@RequestParam String year,@RequestParam String plantFKId) {
		return configurationService.getConfigurationIntermediateValuesData(year,plantFKId);
	}
	
	@PostMapping(value="/saveConfigurationData")
	public List<ConfigurationDTO> saveConfigurationData(@RequestParam String year,@RequestParam String plantFKId, @RequestBody List<ConfigurationDTO> configurationDTOList) {
		configurationService.saveConfigurationData(year,plantFKId,configurationDTOList);
		return configurationDTOList;
	}
	
	@GetMapping(value="/getPeConfigData")
	public  List<Map<String, Object>> getNormAttributeTransactionReceipeSp(@RequestParam String year,@RequestParam String plantId){
		return	 configurationService.getNormAttributeTransactionReceipe(year,plantId);
		
	}
	
	@PostMapping(value="/updatePeConfigData")
	public List<NormAttributeTransactionReceipe> updateCalculatedConsumptionNorms(@RequestParam String year,@RequestParam String plantId,@RequestBody List<NormAttributeTransactionReceipeRequestDTO> normAttributeTransactionReceipeDTOList){
		return configurationService.updateCalculatedConsumptionNorms(year,plantId,normAttributeTransactionReceipeDTOList);
	}
	
	@GetMapping(value="/configuration-constants")
	public AOPMessageVM getConfigurationConstants(@RequestParam String year,@RequestParam String plantFKId) {
		return configurationService.getConfigurationConstants(year,plantFKId);
	}

	@GetMapping(value = "/configuration-constants-export-excel")
	public ResponseEntity<byte[]> exportConfigurationConstantsReport(
	         @RequestParam("plantFKId") String plantFKId,
            @RequestParam("year") String year
	        ) {
	    try {
			
	        byte[] excelBytes = configurationService.createConfigurationConstantsExcel(year,UUID.fromString(plantFKId)); //excelService.generateFlexibleExcel(data, plantId, year);//productionVolumeDataReportExportService.getReportForPlantProductionPlanData(plantId, year, reportType);

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("configuration_constants.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}


	@GetMapping(value = "/configuration-export-excel")
	public ResponseEntity<byte[]> exportConfigurationReport(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year
	        ) {
	    try {
			
	        byte[] excelBytes = configurationService.createExcel(year,UUID.fromString(plantId), false,null); //excelService.generateFlexibleExcel(data, plantId, year);//productionVolumeDataReportExportService.getReportForPlantProductionPlanData(plantId, year, reportType);

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
	
	
	@PostMapping(value = "/configuration-import-excel", consumes = "multipart/form-data")
	public AOPMessageVM importExcel(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,
			@RequestParam("file") MultipartFile file
	        ) {
			return	configurationService.importExcel(year,UUID.fromString(plantId), file); 
	}
	
	@PostMapping(value = "/configuration-constants-import-excel", consumes = "multipart/form-data")
	public AOPMessageVM importConfigurationConstantsExcel(
	         @RequestParam("plantFKId") String plantFKId,
            @RequestParam("year") String year,
			@RequestParam("file") MultipartFile file
	        ) {
		
	        return configurationService.importConfigurationConstantsExcel(year,UUID.fromString(plantFKId), file); //excelService.generateFlexibleExcel(data, plantId, year);//productionVolumeDataReportExportService.getReportForPlantProductionPlanData(plantId, year, reportType);
	}
	
	@GetMapping(value="/configuration-execution")
	public AOPMessageVM getConfigurationExecution(@RequestParam String year,@RequestParam String plantId) {
		return configurationService.getConfigurationExecution(year,plantId);
	}
	
	@PostMapping(value="/configuration-execution")
	public AOPMessageVM saveConfigurationExecution(@RequestBody List<ExecutionDetailDto> executionDetailDtoList) {
		return configurationService.saveConfigurationExecution(executionDetailDtoList);
	}

}
