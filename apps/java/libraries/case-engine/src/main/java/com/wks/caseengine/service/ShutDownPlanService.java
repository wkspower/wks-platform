package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;

import com.wks.caseengine.entity.PlantMaintenanceTransaction;

public interface ShutDownPlanService {
	
	public List<Object[]> findMaintenanceDetailsByPlantIdAndType(UUID plantId,String maintenanceTypeName);
	public UUID findPlantMaintenanceId(String productName);
	public void saveShutdownData(PlantMaintenanceTransaction plantMaintenanceTransaction);
	public UUID findIdByPlantIdAndMaintenanceTypeName(UUID plantId,String maintenanceTypeName);
	public PlantMaintenanceTransaction editShutDownPlanData(UUID plantMaintenanceTransactionId);
	public void deletePlanData(UUID plantMaintenanceTransactionId);

}
