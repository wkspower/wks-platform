package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;
import com.wks.caseengine.dto.ShutDownPlanDTO;

public interface TurnaroundPlanService {
	
	
	public List<ShutDownPlanDTO> findTurnaroundPlanDataByPlantIdAndType(UUID plantId,String maintenanceTypeName);
	public List<ShutDownPlanDTO> saveTurnaroundPlanData(UUID plantId,List<ShutDownPlanDTO> shutDownPlanDTOList);
	public List<ShutDownPlanDTO> editTurnaroundPlanData(UUID plantMaintenanceTransactionId, List<ShutDownPlanDTO> shutDownPlanDTOList);

}
