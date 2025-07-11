package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;
import com.wks.caseengine.dto.NormAttributeTransactionsDTO;
import com.wks.caseengine.dto.ShutDownPlanDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface SlowdownPlanService {
	
	public List<ShutDownPlanDTO> findSlowdownDetailsByPlantIdAndType(UUID plantId,String maintenanceTypeName,String year);
	public List<ShutDownPlanDTO> saveShutdownData( UUID plantId, List<ShutDownPlanDTO> shutDownPlanDTOList);
	public List<ShutDownPlanDTO> editShutdownData(UUID plantMaintenanceTransactionId, List<ShutDownPlanDTO> shutDownPlanDTOList); 
	public AOPMessageVM saveSlowdownConfigurationData( String plantId, String year,  List<NormAttributeTransactionsDTO> normAttributeTransactionsDTOList);
	public AOPMessageVM getSlowdownConfigurationData(String plantId,String year);
	public AOPMessageVM getShutdownDynamicColumns(String auditYear,  UUID plantId);
}
