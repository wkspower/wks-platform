package com.wks.caseengine.rest.server;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.wks.caseengine.dto.BusinessDemandDataDTO;
import com.wks.caseengine.service.BusinessDemandDataService;

@RestController
@RequestMapping("task")
public class BusinessDemandDataController {
	
	@Autowired
	private BusinessDemandDataService businessDemandDataService;
	
	@GetMapping(value="/business-demand-data")
	public	List<BusinessDemandDataDTO> getBusinessDemandData(@RequestParam String year,@RequestParam String plantId){
		System.out.println(plantId);
		return businessDemandDataService.getBusinessDemandData(year,plantId);	
	}
	
	@PostMapping(value="/business-demand-data")
	public 	List<BusinessDemandDataDTO>  saveBusinessDemandData(@RequestBody List<BusinessDemandDataDTO> businessDemandDataDTO) {
		return businessDemandDataService.saveBusinessDemandData(businessDemandDataDTO);
	}
	
	@PutMapping(value="/business-demand-data")
	public List<BusinessDemandDataDTO> editBusinessDemandData(@RequestBody List<BusinessDemandDataDTO> businessDemandDataDTO){
		return businessDemandDataService.saveBusinessDemandData(businessDemandDataDTO);	
	}
	
	@DeleteMapping(value="/business-demand-data/{id}")
	public BusinessDemandDataDTO deleteBusinessDemandData(@PathVariable UUID id){
		return businessDemandDataService.deleteBusinessDemandData(id);	
	}
	
	


}
