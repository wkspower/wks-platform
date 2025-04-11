package com.wks.caseengine.rest.server;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.wks.caseengine.dto.BusinessDemandDataDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.BusinessDemandService;

@RestController
@RequestMapping("/business-demand-data")
public class BusinessDemandController {
	
	@Autowired
	private BusinessDemandService businessDemandDataService;
	

	@GetMapping
	public	ResponseEntity<AOPMessageVM> getBusinessDemandData(@RequestParam String year,@RequestParam String plantId){
			AOPMessageVM response=	 businessDemandDataService.getBusinessDemandData(year,plantId);	
			return ResponseEntity.status(response.getCode()).body(response);
	}
	
	@PostMapping
	public 	ResponseEntity<AOPMessageVM>  saveBusinessDemandData(@RequestBody List<BusinessDemandDataDTO> businessDemandDataDTO) {
		AOPMessageVM response=	 businessDemandDataService.saveBusinessDemandData(businessDemandDataDTO);
		return ResponseEntity.status(response.getCode()).body(response);
	}
	
	@PutMapping
	public ResponseEntity<AOPMessageVM> editBusinessDemandData(@RequestBody List<BusinessDemandDataDTO> businessDemandDataDTO){
		AOPMessageVM response= businessDemandDataService.saveBusinessDemandData(businessDemandDataDTO);	
		return ResponseEntity.status(response.getCode()).body(response);
	}
	
	@DeleteMapping(value="/{id}")
	public BusinessDemandDataDTO deleteBusinessDemandData(@PathVariable UUID id){
		return businessDemandDataService.deleteBusinessDemandData(id);	
	}
	


}
