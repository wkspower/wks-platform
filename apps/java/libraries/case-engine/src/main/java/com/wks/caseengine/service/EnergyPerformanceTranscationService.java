package com.wks.caseengine.service;

import java.util.List;

import com.wks.caseengine.dto.EnergyPerformanceDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface EnergyPerformanceTranscationService {
	
	public AOPMessageVM getEnergyPerformanceTransaction(String siteId,String year);
	public AOPMessageVM saveEnergyPerformanceTransaction( String year, String plantFKId, List<EnergyPerformanceDTO> energyPerformanceDTOs);
	
}
