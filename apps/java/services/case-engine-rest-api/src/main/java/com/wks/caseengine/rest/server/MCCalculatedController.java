package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.AOPMCCalculatedDataDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.MCCalculatedDataService;

@RestController
@RequestMapping("/mc-calculated")
public class MCCalculatedController {
	
	@Autowired
	private MCCalculatedDataService aOPMCCalculatedDataService;
	

	@GetMapping
	public  ResponseEntity<AOPMessageVM> getAOPMCCalculatedData(@RequestParam String plantId, @RequestParam String year){
		AOPMessageVM response=  aOPMCCalculatedDataService.getAOPMCCalculatedData(plantId,year);
		return ResponseEntity.status(response.getCode()).body(response);
	}
	@PutMapping
	public ResponseEntity<AOPMessageVM> editAOPMCCalculatedData(@RequestBody List<AOPMCCalculatedDataDTO> aOPMCCalculatedDataDTO) {
		AOPMessageVM response=  aOPMCCalculatedDataService.editAOPMCCalculatedData(aOPMCCalculatedDataDTO);
		return ResponseEntity.status(response.getCode()).body(response);	
	}
	
	@GetMapping(value="/sp")
	public  int getAOPMCCalculatedDataSP(@RequestParam String plantId, @RequestParam String year){
		return aOPMCCalculatedDataService.getAOPMCCalculatedDataSP(plantId,year);
	}
}
