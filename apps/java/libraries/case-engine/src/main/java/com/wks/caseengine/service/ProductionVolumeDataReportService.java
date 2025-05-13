package com.wks.caseengine.service;

import com.wks.caseengine.message.vm.AOPMessageVM;

public interface ProductionVolumeDataReportService {
	
	public AOPMessageVM getReportForProductionVolumnData(String plantId,String year);
	public AOPMessageVM getReportForMonthWiseProductionData(String plantId,String year);
    public AOPMessageVM getReportForMonthWiseConsumptionSummaryData(String plantId, String year);
    public AOPMessageVM getReportForPlantProductionPlanData(String plantId,String year,String reportType);
    public AOPMessageVM getReportForPlantContributionYearWise(String plantId,String year,String reportType);

}
