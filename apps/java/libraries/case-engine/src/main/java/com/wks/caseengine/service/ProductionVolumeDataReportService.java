package com.wks.caseengine.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import com.wks.caseengine.message.vm.AOPMessageVM;

public interface ProductionVolumeDataReportService {
	
	public AOPMessageVM getReportForProductionVolumnData(String plantId,String year);
	public AOPMessageVM getReportForMonthWiseProductionData(String plantId,String year);
    public AOPMessageVM getReportForMonthWiseConsumptionSummaryData(String plantId, String year);
    public AOPMessageVM getReportForPlantProductionPlanData(String plantId,String year,String reportType);

}
