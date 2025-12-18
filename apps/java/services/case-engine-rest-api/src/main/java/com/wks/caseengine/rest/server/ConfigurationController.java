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
import com.wks.caseengine.dto.ConfigurationVersionDTO;
import com.wks.caseengine.dto.ExecutionDetailDto;
import com.wks.caseengine.dto.NormAttributeTransactionReceipeRequestDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.ConfigurationService;

@RestController
@RequestMapping("task")
public class ConfigurationController {
	
	@Autowired
	private ConfigurationService configurationService;
	
	@GetMapping(value="/production-norms")
	public AOPMessageVM getConfigurationData(@RequestParam String year,@RequestParam UUID plantFKId,@RequestParam(required=false) String version) {
		return configurationService.getConfigurationData(year,plantFKId,version);
	}
	
	@GetMapping(value="/monthly-production-manual-entry")
	public List<ConfigurationDTO> getMonthlyProductionData(@RequestParam String year,@RequestParam UUID plantId) {
		return configurationService.getMonthlyProductionData(year,plantId);
	}
	
	@GetMapping(value="/calculate-steady-norms")
	public AOPMessageVM calculateSteadyNorms(@RequestParam String year,@RequestParam String plantId,@RequestParam(required=false) String periodTo,@RequestParam(required=false) String periodFrom){
		return	configurationService.calculateSteadyNorms(year, plantId,periodTo,periodFrom);
	}
	
	@GetMapping(value="/carry-forward")
	public AOPMessageVM carryForward(@RequestParam String year,@RequestParam String plantId){
		return	configurationService.carryForward(year, plantId);
	}
	
	@GetMapping(value="/configuration/intermediate-values")
	public AOPMessageVM getConfigurationIntermediateValues(@RequestParam String year,@RequestParam UUID plantFKId) {
		return configurationService.getConfigurationIntermediateValues(year,plantFKId);
	}
	
	@GetMapping(value="/intermediate-values")
	public AOPMessageVM getConfigurationIntermediateValuesData(@RequestParam String year,@RequestParam String plantFKId) {
		return configurationService.getConfigurationIntermediateValuesData(year,plantFKId);
	}
	
	@PostMapping(value="/production-norms")
	public List<ConfigurationDTO> saveConfigurationData(@RequestParam String year,@RequestParam String plantFKId,@RequestParam(required=false) String version, @RequestBody List<ConfigurationDTO> configurationDTOList,@RequestParam(required=false) Boolean calculation) {
		configurationService.saveConfigurationData(year,plantFKId,version,configurationDTOList,calculation);
		return configurationDTOList;
	}
	
	@GetMapping(value="/getPeConfigData")
	public  List<Map<String, Object>> getNormAttributeTransactionReceipeSp(@RequestParam String year,@RequestParam String plantId){
		return	 configurationService.getNormAttributeTransactionReceipe(year,plantId);
		
	}
	
	@PostMapping(value="/updatePeConfigData")
	public List<NormAttributeTransactionReceipeRequestDTO> updateCalculatedConsumptionNorms(@RequestParam String year,@RequestParam String plantId,@RequestBody List<NormAttributeTransactionReceipeRequestDTO> normAttributeTransactionReceipeDTOList){
		return configurationService.updateCalculatedConsumptionNorms(year,plantId,normAttributeTransactionReceipeDTOList);
	}
	
	@GetMapping(value="/configuration-constants")
	public AOPMessageVM getConfigurationConstants(@RequestParam String year,@RequestParam String plantFKId) {
		return configurationService.getConfigurationConstants(year,plantFKId);
	}


	@GetMapping(value="/configuration-constants-norms")
	public AOPMessageVM getConfigurationConstantsNorms(@RequestParam String year,@RequestParam String plantFKId) {
		return configurationService.getConfigurationConstantsNorms(year,plantFKId);
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
	
	@GetMapping(value = "/configuration-constants-norms-export-excel")
	public ResponseEntity<byte[]> exportConfigurationConstantsNorms(
	         @RequestParam("plantFKId") String plantId,
            @RequestParam("year") String year
	        ) {
	    try {
			
	        byte[] excelBytes = configurationService.exportConfigurationConstantsNorms(year,plantId); //excelService.generateFlexibleExcel(data, plantId, year);//productionVolumeDataReportExportService.getReportForPlantProductionPlanData(plantId, year, reportType);

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("configuration_constants_norms.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}

	@GetMapping(value = "/recipe-export")
	public ResponseEntity<byte[]> exportConfigData(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year
	        ) {
	    try {
			
	        byte[] excelBytes = configurationService.exportConfigData(year,UUID.fromString(plantId),false,null); //excelService.generateFlexibleExcel(data, plantId, year);//productionVolumeDataReportExportService.getReportForPlantProductionPlanData(plantId, year, reportType);

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("recipe.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	@PostMapping(value = "/recipe-import", consumes = "multipart/form-data")
	public AOPMessageVM importRecipe(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,
			@RequestParam("file") MultipartFile file
	        ) {
			return	configurationService.importRecipe(year,UUID.fromString(plantId), file); 
	}

	@PostMapping(value = "/configuration-export-excel")
	public ResponseEntity<byte[]> exportConfigurationReport(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,@RequestBody(required=false) List<String> reportTypes,
            @RequestParam(required=false) String version
	        ) {
	    try {
			
	        byte[] excelBytes = configurationService.createExcel(year,UUID.fromString(plantId),reportTypes,version, false,null); //excelService.generateFlexibleExcel(data, plantId, year);//productionVolumeDataReportExportService.getReportForPlantProductionPlanData(plantId, year, reportType);

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
	
	@GetMapping(value = "/shutdown-rate-export")
	public ResponseEntity<byte[]> exportShutdownRate(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,
            @RequestParam("type") String type
	        ) {
	    try {
			
	        byte[] excelBytes = configurationService.createShutdownRateExcel(year,UUID.fromString(plantId),type, false,null); //excelService.generateFlexibleExcel(data, plantId, year);//productionVolumeDataReportExportService.getReportForPlantProductionPlanData(plantId, year, reportType);

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("shutdown_rate.xlsx")
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
            @RequestParam("year") String year,@RequestParam(required=false) List<String> reportTypes,@RequestParam(required=false) String version,@RequestParam(required=false) Boolean calculation,
			@RequestParam("file") MultipartFile file
	        ) {
			return	configurationService.importExcel(year,UUID.fromString(plantId),reportTypes,version, file,calculation); 
	}
	
	@PostMapping(value = "/shutdown-rate-import", consumes = "multipart/form-data")
	public AOPMessageVM importShutdownRateExcel(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,
            @RequestParam("type") String type,
            @RequestParam(required=false) String version,
			@RequestParam("file") MultipartFile file,
			@RequestParam(required=false) Boolean calculation
	        ) {
			return	configurationService.importShutdownRateExcel(year,UUID.fromString(plantId),type,version, file,calculation); 
	}
	
	@PostMapping(value = "/configuration-constants-import-excel", consumes = "multipart/form-data")
	public AOPMessageVM importConfigurationConstantsExcel(
	         @RequestParam("plantFKId") String plantFKId,
            @RequestParam("year") String year,
            @RequestParam(required=false) String version,
            @RequestParam(required=false) Boolean calculation,
			@RequestParam("file") MultipartFile file
	        ) {
		
	        return configurationService.importConfigurationConstantsExcel(year,UUID.fromString(plantFKId),version, file,calculation); //excelService.generateFlexibleExcel(data, plantId, year);//productionVolumeDataReportExportService.getReportForPlantProductionPlanData(plantId, year, reportType);
	}
	
	@GetMapping(value="/configuration-execution")
	public AOPMessageVM getConfigurationExecution(@RequestParam String year,@RequestParam String plantId) {
		return configurationService.getConfigurationExecution(year,plantId);
	}
	
	@GetMapping(value="/configuration-execution-norms")
	public AOPMessageVM getConfigurationExecutionNorms(@RequestParam String year,@RequestParam String plantId) {
		return configurationService.getConfigurationExecutionNorms(year,plantId);
	}
	
	@PostMapping(value="/configuration-execution")
	public AOPMessageVM saveConfigurationExecution(@RequestBody List<ExecutionDetailDto> executionDetailDtoList) {
		return configurationService.saveConfigurationExecution(executionDetailDtoList);
	}
	
	@PostMapping(value="/configuration-execution-norms")
	public AOPMessageVM saveConfigurationExecutionNorms(@RequestBody List<ExecutionDetailDto> executionDetailDtoList) {
		return configurationService.saveConfigurationExecutionNorms(executionDetailDtoList);
	}
	
	@GetMapping(value="/configuration-version")
	public AOPMessageVM getConfigurationVersion(@RequestParam String year,@RequestParam String plantId) {
		return configurationService.getConfigurationVersion(year,plantId);
	}
	
	@PostMapping(value="/configuration-version")
	public AOPMessageVM updateConfigurationVersion(@RequestBody List<ConfigurationVersionDTO> configurationVersionDTOs) {
		return configurationService.updateConfigurationVersion(configurationVersionDTOs);
	}


}
