package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.EnergyPerformanceDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.EnergyPerformanceTranscationService;

@RestController
@RequestMapping("task")
public class EnergyPerformanceTranscationController {
	
	@Autowired
	private EnergyPerformanceTranscationService energyPerformanceTranscationService;
	
	@GetMapping(value="/energy-performance")
	public AOPMessageVM getEnergyPerformanceTransaction(@RequestParam String siteId,@RequestParam String year){
		 return  energyPerformanceTranscationService.getEnergyPerformanceTransaction(siteId,year);
	}
	
	@PostMapping(value="/energy-performance")
	public AOPMessageVM saveEnergyPerformanceTransaction(@RequestParam String year,@RequestParam String siteId, @RequestBody List<EnergyPerformanceDTO> energyPerformanceDTOs) {
		return 	energyPerformanceTranscationService.saveEnergyPerformanceTransaction(year,siteId,energyPerformanceDTOs);
	}
	
		
}
