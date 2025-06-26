package com.wks.caseengine.service;

import java.util.List;

import org.springframework.web.bind.annotation.RequestParam;

import com.wks.caseengine.dto.DecokePlanningDTO;
import com.wks.caseengine.dto.MaintenanceCalculatedDataDTO;
import com.wks.caseengine.dto.MaintenanceDetailsDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface MaintenanceCalculatedDataService {
	
	public List<MaintenanceDetailsDTO> getMaintenanceCalculatedData(String plantId, String year);
	public AOPMessageVM getMaintenanceDataForCracker( String plantId,  String year);
	public AOPMessageVM updateMaintenanceDataForCracker( String plantId,  String year, List<DecokePlanningDTO> decokePlanningDTOList);

}
