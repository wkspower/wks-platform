package com.wks.caseengine.rest.server;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.BusinessDemandDataDTO;
import com.wks.caseengine.dto.BusinessDemandMonthlyDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.BusinessDemandDataService;

@RestController
@RequestMapping("task")
public class BusinessDemandDataController {
	
	@Autowired
	private BusinessDemandDataService businessDemandDataService;
	
	@GetMapping(value="/business-demand")
	public	List<BusinessDemandDataDTO> getBusinessDemandData(@RequestParam String year,@RequestParam String plantId){
		System.out.println(plantId);
		return businessDemandDataService.getBusinessDemandData(year,plantId);	
	}
	
	@GetMapping(value="/business-demand-manual-entry")
	public AOPMessageVM getBusinessDemand(@RequestParam String year,@RequestParam UUID plantId) {
		return businessDemandDataService.getBusinessDemand(year,plantId);
	}
	
	@PostMapping(value="/business-demand-manual-entry")
	public AOPMessageVM saveBusinessDemand(@RequestParam String year,@RequestParam String plantId,@RequestBody List<BusinessDemandMonthlyDTO> businessDemandMonthlyDTOs) {
		return businessDemandDataService.saveBusinessDemand(year,plantId,businessDemandMonthlyDTOs);	
	}
	
	@GetMapping(value = "/business-demand-export")
	public ResponseEntity<byte[]> exportBusinessDemand(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year
	        ) {
	    try {
			
	        byte[] excelBytes = businessDemandDataService.exportBusinessDemand(year,plantId,false,null); //excelService.generateFlexibleExcel(data, plantId, year);//productionVolumeDataReportExportService.getReportForPlantProductionPlanData(plantId, year, reportType);

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("Business_demand.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	@PostMapping(value = "/business-demand-import", consumes = "multipart/form-data")
	public AOPMessageVM importExcel(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,
			@RequestParam("file") MultipartFile file
	        ) {
			return	businessDemandDataService.importExcel(year,UUID.fromString(plantId), file); 
	}

	
	@PostMapping(value="/business-demand")
	public 	List<BusinessDemandDataDTO>  saveBusinessDemandData(@RequestBody List<BusinessDemandDataDTO> businessDemandDataDTO) {
		return businessDemandDataService.saveBusinessDemandData(businessDemandDataDTO);
	}
	
	@PutMapping(value="/business-demand")
	public List<BusinessDemandDataDTO> editBusinessDemandData(@RequestBody List<BusinessDemandDataDTO> businessDemandDataDTO){
		return businessDemandDataService.saveBusinessDemandData(businessDemandDataDTO);	
	}
	
	@DeleteMapping(value="/business-demand/{id}")
	public BusinessDemandDataDTO deleteBusinessDemandData(@PathVariable UUID id){
		return businessDemandDataService.deleteBusinessDemandData(id);	
	}
	
	


}
