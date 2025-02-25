package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.wks.caseengine.dto.ShutDownPlanDTO;
import com.wks.caseengine.entity.PlantMaintenanceTransaction;

public interface ShutDownPlanService {
	
	public List<ShutDownPlanDTO> findMaintenanceDetailsByPlantIdAndType(UUID plantId,String maintenanceTypeName);
	public ShutDownPlanDTO saveShutdownPlantData(UUID plantId,ShutDownPlanDTO shutDownPlanDTO);
	public UUID findPlantMaintenanceId(String productName);
	public void saveShutdownData(PlantMaintenanceTransaction plantMaintenanceTransaction);
	public UUID findIdByPlantIdAndMaintenanceTypeName(UUID plantId,String maintenanceTypeName);
	public PlantMaintenanceTransaction editShutDownPlanData(UUID plantMaintenanceTransactionId);
	public void deletePlanData(UUID plantMaintenanceTransactionId);
	public ShutDownPlanDTO editShutdownData(UUID plantMaintenanceTransactionId, ShutDownPlanDTO shutDownPlanDTO);

}
