package com.wks.caseengine.service;

import java.util.List;

import com.wks.caseengine.dto.EnergyPerformanceDTO;
import com.wks.caseengine.dto.ReportCapexPIOPlanDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface ReportCapexPIOPlanService {

	
	public AOPMessageVM getReportCapexPIOPlanTransaction(String siteId,String year);
	public AOPMessageVM saveReportCapexPIOPlanTransaction( String year, String plantFKId, List<ReportCapexPIOPlanDTO> energyPerformanceDTOs);
	
}
