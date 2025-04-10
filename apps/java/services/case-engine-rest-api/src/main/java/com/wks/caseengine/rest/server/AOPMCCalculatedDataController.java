package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.AOPMCCalculatedDataDTO;
import com.wks.caseengine.service.AOPMCCalculatedDataService;

@RestController
@RequestMapping("task")
public class AOPMCCalculatedDataController {
	
	@Autowired
	private AOPMCCalculatedDataService aOPMCCalculatedDataService;
	
	@GetMapping(value="/aop-mc-calculated-data")
	public  List<AOPMCCalculatedDataDTO> getAOPMCCalculatedData(@RequestParam String plantId, @RequestParam String year){
		return aOPMCCalculatedDataService.getAOPMCCalculatedData(plantId,year);
	}
	@PutMapping(value="/aop-mc-calculated-data")
	public List<AOPMCCalculatedDataDTO> editAOPMCCalculatedData(@RequestBody List<AOPMCCalculatedDataDTO> aOPMCCalculatedDataDTO) {
		return aOPMCCalculatedDataService.editAOPMCCalculatedData(aOPMCCalculatedDataDTO);
	}
	
	@GetMapping(value="/aop-mc-calculated-data/calculate")
	public  int getAOPMCCalculatedDataSP(@RequestParam String plantId, @RequestParam String year){
		return aOPMCCalculatedDataService.getAOPMCCalculatedDataSP(plantId,year);
	}
}
