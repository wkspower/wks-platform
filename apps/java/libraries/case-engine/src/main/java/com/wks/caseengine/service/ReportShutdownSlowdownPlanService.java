package com.wks.caseengine.service;

import java.util.List;


import com.wks.caseengine.dto.ShutdownSlowdownPlanDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface ReportShutdownSlowdownPlanService {

	
	public AOPMessageVM getShutdownSlowdownPlan(String siteId,String year);
	public AOPMessageVM saveShutdownSlowdownPlan( String year, String plantFKId, List<ShutdownSlowdownPlanDTO> shutdownSlowdownPlanDTOs);
	
}
