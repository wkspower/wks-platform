package com.wks.caseengine.service;

import com.wks.caseengine.message.vm.AOPMessageVM;

public interface AOPReportService {
	
	public AOPMessageVM getAnnualAOPReport( String plantId, String year, String reportType,String AopYearFilter);
	public AOPMessageVM getReportForProductionVolumnData(String plantId, String year, String reportType);

}
