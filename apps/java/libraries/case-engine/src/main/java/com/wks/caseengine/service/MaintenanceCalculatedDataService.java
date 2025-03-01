package com.wks.caseengine.service;

import java.util.List;

import org.springframework.web.bind.annotation.RequestParam;

import com.wks.caseengine.dto.MaintenanceCalculatedDataDTO;

public interface MaintenanceCalculatedDataService {
	
	public List<MaintenanceCalculatedDataDTO> getMaintenanceCalculatedData(String plantId, String year);

}
