package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;
import com.wks.caseengine.dto.ShutDownPlanDTO;

public interface TurnaroundPlanService {
	
	
	public List<ShutDownPlanDTO> findTurnaroundPlanDataByPlantIdAndType(UUID plantId,String maintenanceTypeName);
	public ShutDownPlanDTO saveTurnaroundPlanData(UUID plantId,ShutDownPlanDTO shutDownPlanDTO);
	public ShutDownPlanDTO editTurnaroundPlanData(UUID plantMaintenanceTransactionId, ShutDownPlanDTO shutDownPlanDTO);

}
