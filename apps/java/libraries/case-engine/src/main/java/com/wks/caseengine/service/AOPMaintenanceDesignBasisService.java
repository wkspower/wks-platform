package com.wks.caseengine.service;

import java.util.List;


import com.wks.caseengine.dto.AOPMaintenanceDesignRemarksDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface AOPMaintenanceDesignBasisService {
	
	public AOPMessageVM getMaintenanceDesignBasis(String plantId, String year);
	public AOPMessageVM updateMaintenanceDesignBasis(String plantId, String year,List<AOPMaintenanceDesignRemarksDTO> aopMaintenanceDesignRemarksDTOs);
	
}
