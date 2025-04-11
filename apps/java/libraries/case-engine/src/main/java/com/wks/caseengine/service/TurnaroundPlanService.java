package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;
import com.wks.caseengine.dto.ShutDownPlanDTO;

public interface TurnaroundPlanService {
	
	
	public List<ShutDownPlanDTO> getPlans(UUID plantId,String maintenanceTypeName, String year);
	public List<ShutDownPlanDTO> savePlans(UUID plantId,List<ShutDownPlanDTO> shutDownPlanDTOList);
	public List<ShutDownPlanDTO> updatePlans(UUID plantMaintenanceTransactionId, List<ShutDownPlanDTO> shutDownPlanDTOList);

}
