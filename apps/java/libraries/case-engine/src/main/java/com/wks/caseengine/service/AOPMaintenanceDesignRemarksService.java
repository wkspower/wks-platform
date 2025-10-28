package com.wks.caseengine.service;

import com.wks.caseengine.message.vm.AOPMessageVM;

public interface AOPMaintenanceDesignRemarksService {
	
	public AOPMessageVM getMaintenanceDesignRemarks(String plantId, String year);
	public AOPMessageVM updateMaintenanceDesignRemarks(String plantId, String year,String summary);
	
}
