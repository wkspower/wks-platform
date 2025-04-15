package com.wks.caseengine.service;

import java.util.List;

import org.springframework.web.bind.annotation.RequestParam;

import com.wks.caseengine.dto.MaintenanceCalculatedDataDTO;
import com.wks.caseengine.dto.MaintenanceDetailsDTO;

public interface MaintenanceCalculatedService {

	public List<MaintenanceDetailsDTO> getMaintenanceCalculated(String plantId, String year);

}
