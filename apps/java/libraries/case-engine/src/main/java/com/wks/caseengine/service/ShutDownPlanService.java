package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;


import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.MonthWiseDataDTO;
import com.wks.caseengine.dto.ShutDownPlanDTO;
import com.wks.caseengine.entity.PlantMaintenanceTransaction;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface ShutDownPlanService {
	
	public List<ShutDownPlanDTO> findMaintenanceDetailsByPlantIdAndType(UUID plantId,String maintenanceTypeName, String year);
	byte[] shutdownExport(String year, String plantId,String maintenanceTypeName, boolean isAfterSave,
			List<ShutDownPlanDTO> mapForExcel);
	byte[] shutdownNonProductExport(String year, String plantId,String maintenanceTypeName, boolean isAfterSave,
			List<ShutDownPlanDTO> mapForExcel);
	byte[] shutdownNonProductLineExport(String year, String plantId,String maintenanceTypeName, boolean isAfterSave,
			List<ShutDownPlanDTO> mapForExcel);
	public AOPMessageVM importShutdownExcel(String year,UUID plantId, String maintenanceTypeName,MultipartFile file);
	public AOPMessageVM importLineShutdown(String year,UUID plantId, String maintenanceTypeName,MultipartFile file);
	public AOPMessageVM importNonProductShutdown(String year,UUID plantId, String maintenanceTypeName,MultipartFile file);
	public List<ShutDownPlanDTO> saveShutdownPlantData(UUID plantId,List<ShutDownPlanDTO> shutDownPlanDTOList);
	public UUID findPlantMaintenanceId(String productName);
	public void saveShutdownData(PlantMaintenanceTransaction plantMaintenanceTransaction);
	public UUID findIdByPlantIdAndMaintenanceTypeName(UUID plantId,String maintenanceTypeName);
	public PlantMaintenanceTransaction editShutDownPlanData(UUID plantMaintenanceTransactionId);
	public void deletePlanData(UUID plantMaintenanceTransactionId,UUID plantId);
	public List<ShutDownPlanDTO> editShutdownData(UUID plantMaintenanceTransactionId, List<ShutDownPlanDTO> shutDownPlanDTOList);
	public List<MonthWiseDataDTO> getMonthlyShutdownHours(String auditYear,  UUID plantId);
	public AOPMessageVM getDescriptionDropdown(String plantId);
	public AOPMessageVM getShutdownDescription(String plantId);
	
	void deleteShutPlanData(UUID plantMaintenanceTransactionId, UUID plantId);
	
}
