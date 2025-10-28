package com.wks.caseengine.service;

import java.util.List;


import com.wks.caseengine.dto.AOPMaintenanceDesignRemarksDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface AOPMaintenanceDesignRemarksService {
	
	public AOPMessageVM getMaintenanceDesignRemarks(String plantId, String year);
	public AOPMessageVM updateMaintenanceDesignRemarks(String plantId, String year,List<AOPMaintenanceDesignRemarksDTO> aopMaintenanceDesignRemarksDTOs);
	
}
