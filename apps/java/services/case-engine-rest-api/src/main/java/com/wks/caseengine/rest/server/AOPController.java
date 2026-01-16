package com.wks.caseengine.rest.server;

import java.util.List;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.wks.caseengine.dto.AOPDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.AOPService;

@RestController
@RequestMapping("task")
public class AOPController {
	
	@Autowired
	private AOPService aopService;
	
	@GetMapping(value="/monthly-production")
	public AOPMessageVM getAOP(@RequestParam String plantId,@RequestParam String year,@RequestParam(required=false) String type){
		 return  aopService.getAOPData(plantId,year,type);
	}
	
	@GetMapping(value = "/monthly-production-export")
	public ResponseEntity<byte[]> exportAOPData(@RequestParam String plantId,@RequestParam String year,@RequestParam(required=false) String type) {
	    try {
			
	        byte[] excelBytes = aopService.exportAOPData(plantId,year,type,false,null); //excelService.generateFlexibleExcel(data, plantId, year);//productionVolumeDataReportExportService.getReportForPlantProductionPlanData(plantId, year, reportType);

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("Monthly_Production.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	@PutMapping(value="/monthly-production")
	public List<AOPDTO> updateAOP(@RequestBody List<AOPDTO> aOPDTOList) {
		aopService.updateAOP(aOPDTOList);
		return aOPDTOList;
	}

    @GetMapping(value="/calculate-monthly-production")
	public AOPMessageVM calculateData(@RequestParam String plantId,@RequestParam String year){
    	try {
    		 return aopService.calculateData(plantId,year);
    		// return ResponseEntity.ok(aOPList);
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
		return null;
	}
    
    @GetMapping(value = "/aop-years")
    public ResponseEntity<List<Map<String, String>>> getYears() {
        List<Map<String, String>> data = aopService.getAOPYears();
        return ResponseEntity.ok(data);
    }

  
}

