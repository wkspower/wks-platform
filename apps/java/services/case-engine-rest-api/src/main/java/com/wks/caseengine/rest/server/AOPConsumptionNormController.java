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

import com.wks.caseengine.dto.CalculatedConsumptionNormsDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.dto.AOPConsumptionNormDTO;
import com.wks.caseengine.service.AOPConsumptionNormService;

@RestController
@RequestMapping("task")
public class AOPConsumptionNormController {
	
	@Autowired
	private AOPConsumptionNormService aopConsumptionNormService;
	
	@GetMapping(value="/overall-consumption")
	public AOPMessageVM getAOPConsumptionNorm(@RequestParam String plantId,@RequestParam String year,@RequestParam(required = false) String gradeId){
		return aopConsumptionNormService.getAOPConsumptionNorm(plantId,year,gradeId);
	}
	
	@GetMapping(value = "/overall-consumption-export")
	public ResponseEntity<byte[]> exportOverallConsumption(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year) {
	    try {
			
	        byte[] excelBytes = aopConsumptionNormService.exportOverallConsumption(year,UUID.fromString(plantId),false,null); //excelService.generateFlexibleExcel(data, plantId, year);//productionVolumeDataReportExportService.getReportForPlantProductionPlanData(plantId, year, reportType);

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("overall-consumption.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}

	@PostMapping(value="/overall-consumption")
	public List<AOPConsumptionNormDTO> saveAOPConsumptionNorm(@RequestBody List<AOPConsumptionNormDTO> aOPConsumptionNormDTOList){
		return aopConsumptionNormService.saveAOPConsumptionNorm(aOPConsumptionNormDTOList);
	}

	@GetMapping(value="/calculate-overall-consumption")
	public AOPMessageVM getNormalOperationNormsDataFromSP(@RequestParam String year,@RequestParam String plantId){
		return	 aopConsumptionNormService.calculateExpressionConsumptionNorms(year,plantId);	
	}
	
	@GetMapping(value="/getCalculatedConsumptionNorms")
	public  List<CalculatedConsumptionNormsDTO>  getCalculatedConsumptionNorms(@RequestParam String year,@RequestParam String plantId){
		return	 aopConsumptionNormService.getCalculatedConsumptionNorms(year,plantId);
	}
	
	@GetMapping(value="/consumption-aop/grades")
	public AOPMessageVM getConsumptionAOPGrades(@RequestParam String year,@RequestParam String plantId){
		return	aopConsumptionNormService.getConsumptionAOPGrades(year, plantId);
	}
	

}

