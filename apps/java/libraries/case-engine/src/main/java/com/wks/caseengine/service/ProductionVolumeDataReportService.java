package com.wks.caseengine.service;

import com.wks.caseengine.message.vm.AOPMessageVM;

public interface ProductionVolumeDataReportService {
	
	public AOPMessageVM getReportForProductionVolumnData(String plantId,String year,String type,String filter);
	public AOPMessageVM getReportForMonthWiseProductionData(String plantId,String year,String typeOne,String typeSecond,String filter);

}
