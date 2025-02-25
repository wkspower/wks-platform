package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;

import com.wks.caseengine.dto.ShutDownPlanDTO;
import com.wks.caseengine.dto.SlowDownPlanDTO;

public interface SlowdownPlanService {
	
	public List<SlowDownPlanDTO> findSlowdownDetailsByPlantIdAndType(UUID plantId,String maintenanceTypeName);
	public ShutDownPlanDTO saveShutdownData( UUID plantId, ShutDownPlanDTO shutDownPlanDTO);
	public ShutDownPlanDTO editShutdownData(UUID plantMaintenanceTransactionId, ShutDownPlanDTO shutDownPlanDTO); 

}
