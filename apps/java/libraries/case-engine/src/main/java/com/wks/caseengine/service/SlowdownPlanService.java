package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;

import com.wks.caseengine.dto.ShutDownPlanDTO;
import com.wks.caseengine.dto.SlowDownPlanDTO;

public interface SlowdownPlanService {
	
	public List<SlowDownPlanDTO> findSlowdownDetailsByPlantIdAndType(UUID plantId,String maintenanceTypeName,String year);
	public List<ShutDownPlanDTO> saveShutdownData( UUID plantId, List<ShutDownPlanDTO> shutDownPlanDTOList);
	public List<ShutDownPlanDTO> editShutdownData(UUID plantMaintenanceTransactionId, List<ShutDownPlanDTO> shutDownPlanDTOList); 

}
