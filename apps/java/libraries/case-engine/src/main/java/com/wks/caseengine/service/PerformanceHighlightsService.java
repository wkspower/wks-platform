package com.wks.caseengine.service;

import java.util.List;

import com.wks.caseengine.dto.EnergyPerformanceDTO;
import com.wks.caseengine.dto.PerformanceHighlightDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface PerformanceHighlightsService {
	
	public AOPMessageVM getPerformanceHighlights(String siteId,String year);
	public AOPMessageVM savePerformanceHighlights( String year, String plantFKId, List<PerformanceHighlightDTO> performanceHighlightDTOs);
	
}
