package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;

import com.wks.caseengine.dto.MonthWiseDataDTO;
import com.wks.caseengine.dto.ShutDownPlanDTO;
import com.wks.caseengine.entity.PlantMaintenanceTransaction;

public interface ShutDownPlanService {
	
	public List<ShutDownPlanDTO> findMaintenanceDetailsByPlantIdAndType(UUID plantId,String maintenanceTypeName, String year);
	public List<ShutDownPlanDTO> saveShutdownPlantData(UUID plantId,List<ShutDownPlanDTO> shutDownPlanDTOList);
	public UUID findPlantMaintenanceId(String productName);
	public void saveShutdownData(PlantMaintenanceTransaction plantMaintenanceTransaction);
	public UUID findIdByPlantIdAndMaintenanceTypeName(UUID plantId,String maintenanceTypeName);
	public PlantMaintenanceTransaction editShutDownPlanData(UUID plantMaintenanceTransactionId);
	public void deletePlanData(UUID plantMaintenanceTransactionId);
	public List<ShutDownPlanDTO> editShutdownData(UUID plantMaintenanceTransactionId, List<ShutDownPlanDTO> shutDownPlanDTOList);
	public List<MonthWiseDataDTO> getMonthlyShutdownHours(String auditYear,  UUID plantId);
	void deleteShutPlanData(UUID plantMaintenanceTransactionId, UUID plantId);

}
