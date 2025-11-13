package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.NormAttributeTransactionsDTO;
import com.wks.caseengine.dto.ShutDownPlanDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface SlowdownPlanService {
	
	public List<ShutDownPlanDTO> findSlowdownDetailsByPlantIdAndType(UUID plantId,String maintenanceTypeName,String year);
	byte[] slowdownExport(String year, String plantId,String maintenanceTypeName, boolean isAfterSave,
			List<ShutDownPlanDTO> mapForExcel);
	byte[] slowdownRateExport(String year, String plantId,String maintenanceTypeName, boolean isAfterSave,
			List<ShutDownPlanDTO> mapForExcel);
	byte[] nonProductSlowdownExport(String year, String plantId,String maintenanceTypeName, boolean isAfterSave,
			List<ShutDownPlanDTO> mapForExcel);
	public AOPMessageVM importSlowdownExcel(String year,UUID plantId, String maintenanceTypeName,MultipartFile file);
	public AOPMessageVM importSlowdownRateExcel(String year,UUID plantId, String maintenanceTypeName,MultipartFile file);
	public AOPMessageVM importNonProductSlowdown(String year,UUID plantId, String maintenanceTypeName,MultipartFile file);
	public List<ShutDownPlanDTO> saveShutdownData( UUID plantId, List<ShutDownPlanDTO> shutDownPlanDTOList);
	public List<ShutDownPlanDTO> editShutdownData(UUID plantMaintenanceTransactionId, List<ShutDownPlanDTO> shutDownPlanDTOList); 
	public AOPMessageVM saveSlowdownConfigurationData( String plantId, String year,  List<NormAttributeTransactionsDTO> normAttributeTransactionsDTOList);
	public AOPMessageVM getSlowdownConfigurationData(String plantId,String year);
	public AOPMessageVM getShutdownDynamicColumns(String auditYear,  UUID plantId);
	public List<ShutDownPlanDTO> saveRampUpData( UUID plantId, List<ShutDownPlanDTO> shutDownPlanDTOList);
	public List<ShutDownPlanDTO> saveRampDownData( UUID plantId, List<ShutDownPlanDTO> shutDownPlanDTOList);
}
