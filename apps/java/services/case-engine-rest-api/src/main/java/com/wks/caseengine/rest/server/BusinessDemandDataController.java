package com.wks.caseengine.rest.server;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
	
	@GetMapping(value="/getBusinessDemandData")
	public	List<BusinessDemandDataDTO> getBusinessDemandData(@RequestParam String year,@RequestParam String plantId){
		return businessDemandDataService.getBusinessDemandData(year,plantId);	
	}
	
	@PostMapping(value="/saveBusinessDemandData")
	public 	List<BusinessDemandDataDTO>  saveBusinessDemandData(@RequestBody List<BusinessDemandDataDTO> businessDemandDataDTO) {
		return businessDemandDataService.saveBusinessDemandData(businessDemandDataDTO);
	}
	
	@PutMapping(value="/editBusinessDemandData")
	public List<BusinessDemandDataDTO> editBusinessDemandData(@RequestBody List<BusinessDemandDataDTO> businessDemandDataDTO){
		return businessDemandDataService.saveBusinessDemandData(businessDemandDataDTO);	
	}
	
	@DeleteMapping(value="/deleteBusinessDemandData")
	public BusinessDemandDataDTO deleteBusinessDemandData(@RequestBody BusinessDemandDataDTO businessDemandDataDTO){
		return businessDemandDataService.deleteBusinessDemandData(businessDemandDataDTO);	
	}
	
	


}
