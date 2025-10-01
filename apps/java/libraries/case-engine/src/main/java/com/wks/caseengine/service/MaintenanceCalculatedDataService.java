package com.wks.caseengine.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.BudgetMaintenanceDto;
import com.wks.caseengine.dto.DecokePlanningDTO;
import com.wks.caseengine.dto.MaintenanceDetailsDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface MaintenanceCalculatedDataService {
	
	public List<MaintenanceDetailsDTO> getMaintenanceCalculatedData(String plantId, String year);
	public AOPMessageVM getMaintenanceDataForCracker( String plantId,  String year);
	public AOPMessageVM updateMaintenanceDataForCracker( String plantId,  String year, List<DecokePlanningDTO> decokePlanningDTOList);
	public AOPMessageVM getBudgetMaintenance( String plantId,  String year,String budgetCategory);
	public AOPMessageVM updateBudgetMaintenance( List<BudgetMaintenanceDto> budgetMaintenanceDtos);
	public AOPMessageVM getMacroData( Double value,  String year,String plantId);
	byte[] createExcel(String year, String plantId,  boolean isAfterSave,
			Map<String, List<BudgetMaintenanceDto>> mapForExcel);
	AOPMessageVM importExcel(String year, String plantFKId, String budgetCategory, MultipartFile file);
	byte[] maintenanceExport(String year, String plantId, boolean isAfterSave,
			List<DecokePlanningDTO> mapForExcel);
	public AOPMessageVM maintenanceImport(String year,UUID plantId,MultipartFile file);
}
