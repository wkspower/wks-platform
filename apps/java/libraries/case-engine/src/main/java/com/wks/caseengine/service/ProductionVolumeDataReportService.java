package com.wks.caseengine.service;

import java.util.List;

import com.wks.caseengine.dto.PlantProductionDataDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface ProductionVolumeDataReportService {

    public AOPMessageVM getReportForProductionVolumnData(String plantId, String year);

    public AOPMessageVM getReportForMonthWiseProductionData(String plantId, String year);

    public AOPMessageVM getReportForMonthWiseConsumptionSummaryData(String plantId, String year);

    public AOPMessageVM getReportForPlantProductionPlanData(String plantId, String year, String reportType);

    public AOPMessageVM getReportForPlantContributionYearWise(String plantId, String year, String reportType);

    // New method added
    public AOPMessageVM savePlantProductionData(String plantId, String year, List<PlantProductionDataDTO> dataList);
     AOPMessageVM calculateProductionSummary(String year, String plantId);

}
