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

import com.wks.caseengine.dto.MCUNormsValueDTO;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.service.NormalOperationNormsService;

@RestController
@RequestMapping("task")
public class NormalOperationNormsController {
	
	@Autowired
	private NormalOperationNormsService normalOperationNormsService;
	
	@Autowired
	PlantsRepository plantsRepository;

	@Autowired
	SiteRepository siteRepository;

	@Autowired
	VerticalsRepository verticalRepository;
	
	@GetMapping(value = "/steady-state-norms")
	public AOPMessageVM getNormalOperationNormsData(
	        @RequestParam String year,
	        @RequestParam String plantId,
	        @RequestParam(required = false) String gradeId,@RequestParam(required = false) String mode) {
	    return normalOperationNormsService.getNormalOperationNormsData(year, plantId, gradeId,mode);
	}
	
	@GetMapping(value = "/steady-state-norms-dynamic")
	public AOPMessageVM getSteadyStateNorms(
	        @RequestParam String year,
	        @RequestParam String plantId,
	        @RequestParam(required = false) String gradeId,@RequestParam(required = false) String mode) {
	    return normalOperationNormsService.getSteadyStateNorms(year, plantId, gradeId,mode);
	}
	
	@GetMapping(value = "/steady-state-norms-export-dynamic")
	public ResponseEntity<byte[]> exportSteadyStateNorms(
	        @RequestParam String year,
	        @RequestParam String plantId) {
	    try {
	        // Calls service to generate byte array Excel file
	        byte[] excelBytes = normalOperationNormsService.exportSteadyStateNormsDynamic(year, plantId, false, null);

	        if (excelBytes == null || excelBytes.length == 0) {
	            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	        }

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("steady_state_norms.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	@PostMapping(value = "/steady-state-norms-import-dynamic", consumes = "multipart/form-data")
	public AOPMessageVM importSteadyStateNorms(
	        @RequestParam("plantId") String plantId,
	        @RequestParam("year") String year,
	        @RequestParam("file") MultipartFile file
	) {
	    return normalOperationNormsService.importSteadyStateNorms(year, plantId, file);
	}
	
	@PostMapping(value="/steady-state-norms-dynamic")
	public AOPMessageVM updateSteadyStateNorms(@RequestParam String plantId, @RequestParam String year, @RequestBody List<Map<String, Object>> payloadList){
		//return normalOperationNormsService.updateSteadyStateNorms(plantId,year,payloadList);		
		return normalOperationNormsService.updateAOPConsumptionNormGrade(plantId,year,payloadList);
	}
	
	@GetMapping(value="/normal-operation/norms/grades")
	public AOPMessageVM getNormalOperationNormsGrades(@RequestParam String year,@RequestParam String plantId){
		return	normalOperationNormsService.getNormalOperationNormsGrades(year, plantId);
	}
	
	@GetMapping(value="/calculate-normal-ops-norms")
	public AOPMessageVM calculateNormalOpsNorms(@RequestParam String aopYear,@RequestParam String plantId,@RequestParam String siteId,@RequestParam String verticalId){
		return	normalOperationNormsService.calculateNormalOpsNorms(aopYear, plantId,siteId,verticalId);
	}

	@GetMapping(value="/calculate-normal-ops-norms/polyester")
	public AOPMessageVM calculateNormalOpsNormsPolyester(@RequestParam String aopYear,@RequestParam String plantId,@RequestParam String siteId,@RequestParam String verticalId){
		return	normalOperationNormsService.calculateNormalOpsNormsPolyester(aopYear, plantId,siteId,verticalId);
	}

	@GetMapping(value = "/norms-transactions")
	public AOPMessageVM getNormsTransaction(@RequestParam String plantId, @RequestParam String year) {
		return normalOperationNormsService.getNormsTransaction(plantId, year);
	}
	
	@GetMapping(value = "/norms-transactions-final-norms-mode-wise")
	public AOPMessageVM getNormsTransactionFinalNormsModeWise(@RequestParam String plantId, @RequestParam String year) {
		return normalOperationNormsService.getNormsTransactionFinalNormsModeWise(plantId, year);
	}
	
	@GetMapping(value = "/norms-transactions-final-norms")
	public AOPMessageVM getNormsTransactionFinalNorms(@RequestParam String plantId, @RequestParam String year) {
		return normalOperationNormsService.getNormsTransactionFinalNorms(plantId, year);
	}

	@PostMapping(value = "/steady-state-norms/polyester")
	public AOPMessageVM saveNormalOperationNormsDataPolyester(
	        @RequestParam String plantId, @RequestParam String year,
	        @RequestParam(required = false) String gradeId,
	        @RequestBody List<MCUNormsValueDTO> mCUNormsValueDTOList) {
	    try {
	        return normalOperationNormsService.saveNormalOperationNormsDataPolyester(
	                mCUNormsValueDTOList, UUID.fromString(plantId), year, gradeId, false);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return AOPMessageVM.builder()
	                .code(500)
	                .message("Failed to save data: " + e.getMessage())
	                .data(null)
	                .build();
	    }
	}
	@GetMapping(value = "/steady-state-norms/grade/validation")
	public AOPMessageVM checkAllGradeNormsPolyester(
	        @RequestParam String plantId, @RequestParam String year,
	        @RequestParam(required = false) String gradeId
	       ) {
	    try {
	        return normalOperationNormsService.checkAllGradeNormsPolyester(
	                UUID.fromString(plantId), year, gradeId);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return AOPMessageVM.builder()
	                .code(500)
	                .message("Failed to save data: " + e.getMessage())
	                .data(null)
	                .build();
	    }
	}
	@PostMapping(value = "/steady-state-norms")
	public List<MCUNormsValueDTO> saveNormalOperationNormsData(
		@RequestParam String plantId, @RequestParam String year,
        @RequestParam(required = false) String gradeId,
			@RequestBody List<MCUNormsValueDTO> mCUNormsValueDTOList) {
		try {
			return normalOperationNormsService.saveNormalOperationNormsData(mCUNormsValueDTOList,UUID.fromString(plantId),year, gradeId,false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@GetMapping(value = "/calculate-steady-state-norms")
	public AOPMessageVM getNormalOperationNormsDataFromSP(@RequestParam String year, @RequestParam String plantId) {
		return normalOperationNormsService.calculateExpressionConsumptionNorms(year, plantId);
	}
	
	@GetMapping(value = "/load-grade-wise-consumption-norms")
	public AOPMessageVM loadGradeWiseConsumptionNorms(@RequestParam String year, @RequestParam String plantId) {
		return normalOperationNormsService.loadGradeWiseConsumptionNorms(year, plantId);
	}
	
	@GetMapping(value = "/steady-state-norms-export")
	public ResponseEntity<byte[]> exportPlantProductionPlanReport(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,@RequestParam(required = false) String mode, @RequestParam(required = false) String gradeId
	        ) {
	    try {
	    	Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			byte[] excelBytes;
			if(vertical.getName().equalsIgnoreCase("Coker") || vertical.getName().equalsIgnoreCase("CRUDE") 
					|| vertical.getName().equalsIgnoreCase("MEROX") || vertical.getName().equalsIgnoreCase("VGOHT") 
					|| vertical.getName().equalsIgnoreCase("PCG") || vertical.getName().equalsIgnoreCase("FCC") || vertical.getName().equalsIgnoreCase("RefineryUtility")
					|| vertical.getName().equalsIgnoreCase("Filament") || vertical.getName().equalsIgnoreCase("Staple")) {
					
				 excelBytes = normalOperationNormsService.createExcelSAP(year,UUID.fromString(plantId),false,null,mode,gradeId); //excelService.generateFlexibleExcel(data, plantId, year);//productionVolumeDataReportExportService.getReportForPlantProductionPlanData(plantId, year, reportType);
			}else {
				 excelBytes = normalOperationNormsService.createExcel(year,UUID.fromString(plantId),false,null,mode,gradeId); //excelService.generateFlexibleExcel(data, plantId, year);//productionVolumeDataReportExportService.getReportForPlantProductionPlanData(plantId, year, reportType);
			}
	        
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

	@GetMapping(value = "/steady-state-norms-export-chemical")
	public ResponseEntity<byte[]> exportSteadyStateNormsChemical(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year
	        ) {
	    try {
			
	        byte[] excelBytes = normalOperationNormsService.exportSteadyStateNormsChemical(year,UUID.fromString(plantId),false,null); 
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

	@GetMapping(value = "/steady-state-norms-all-grades-export")
	public ResponseEntity<byte[]> exportSteadyStateNorms(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,@RequestParam(required = false) String mode) {
	    try {
			
	        byte[] excelBytes = normalOperationNormsService.exportSteadyStateNorms(year,UUID.fromString(plantId),false,null,mode); //excelService.generateFlexibleExcel(data, plantId, year);//productionVolumeDataReportExportService.getReportForPlantProductionPlanData(plantId, year, reportType);

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

	@PostMapping(value = "/steady-state-norms-import/polyester", consumes = "multipart/form-data")
	public AOPMessageVM importExcelPolyester(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,
            @RequestParam(required = false) String gradeId,
			@RequestParam("file") MultipartFile file,@RequestParam(required = false) String mode
	        ) {
			return	normalOperationNormsService.importExcelPolyester(year,UUID.fromString(plantId),gradeId, file,mode); 
	}
	
	@PostMapping(value = "/steady-state-norms-import", consumes = "multipart/form-data")
	public AOPMessageVM importExcel(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,
            @RequestParam(required = false) String gradeId,
			@RequestParam("file") MultipartFile file,@RequestParam(required = false) String mode
	        ) {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			if(vertical.getName().equalsIgnoreCase("Coker") || vertical.getName().equalsIgnoreCase("CRUDE") || vertical.getName().equalsIgnoreCase("MEROX") || vertical.getName().equalsIgnoreCase("VGOHT") || vertical.getName().equalsIgnoreCase("PCG") || vertical.getName().equalsIgnoreCase("FCC") || vertical.getName().equalsIgnoreCase("RefineryUtility")) {
				return	normalOperationNormsService.importExcelSAP(year,UUID.fromString(plantId),gradeId, file,mode); 
			}else {
				return	normalOperationNormsService.importExcel(year,UUID.fromString(plantId),gradeId, file,mode); 
			}
	}
	
	@PostMapping(value = "/steady-state-norms-import-chemical", consumes = "multipart/form-data")
	public AOPMessageVM importChemicalExcel(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,
			@RequestParam("file") MultipartFile file
	        ) {
			return	normalOperationNormsService.importChemicalExcel(year,UUID.fromString(plantId), file); 
	}

	@GetMapping(value = "/catalyst-chemicals-calculation-data")
	public AOPMessageVM getCatChemCalculationData(@RequestParam String plantId, @RequestParam String year) {
		return normalOperationNormsService.getCatChemCalculationData(plantId, year);
	}

	@PostMapping(value = "/save-catalyst-chemicals-calculation-data")
	public AOPMessageVM saveCatChemCalculationData(
			@RequestParam String plantId,
			@RequestParam String year,
			@RequestBody List<Map<String, Object>> payload) {
		return normalOperationNormsService.saveCatChemCalculationData(plantId, year, payload);
	}
	
}

