package com.wks.caseengine.service;

import java.util.List;

import org.springframework.web.bind.annotation.RequestParam;

import com.wks.caseengine.dto.MaintenanceCalculatedDataDTO;
import com.wks.caseengine.dto.MaintenanceDetailsDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface MaintenanceCalculatedService {

	public AOPMessageVM getMaintenanceCalculated(String plantId, String year);

}
