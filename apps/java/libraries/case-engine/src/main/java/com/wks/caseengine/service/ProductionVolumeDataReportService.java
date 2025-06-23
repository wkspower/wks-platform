package com.wks.caseengine.service;

import java.util.List;



import com.wks.caseengine.dto.MonthWiseConsumptionSummaryDTO;
import com.wks.caseengine.dto.MonthWiseProductionPlanDTO;
import com.wks.caseengine.dto.PlantProductionDTO;
import com.wks.caseengine.dto.PlantProductionDataDTO;
import com.wks.caseengine.dto.TurnAroundPlanReportDTO;
import com.wks.caseengine.dto.YearWiseContributionDataDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface ProductionVolumeDataReportService {

    public AOPMessageVM getReportForProductionVolumnData(String plantId, String year);

    public AOPMessageVM getReportForMonthWiseProductionData(String plantId, String year);

    public AOPMessageVM getReportForMonthWiseConsumptionSummaryData(String plantId, String year,String reportType);

    public AOPMessageVM getReportForPlantProductionPlanData(String plantId, String year, String reportType);
    
    public AOPMessageVM updateReportForPlantProductionPlanData(String plantId,String year,List<PlantProductionDTO> dataList);
			
    public AOPMessageVM getReportForPlantContributionYearWise(String plantId, String year, String reportType);
    public AOPMessageVM updateReportForPlantContributionYearWise( String plantId,
			 String year, List<YearWiseContributionDataDTO> dataList); 

    // New method added
    public AOPMessageVM savePlantProductionData(String plantId, String year, List<PlantProductionDataDTO> dataList);

    AOPMessageVM calculateProductionSummary(String year, String plantId);

    AOPMessageVM calculateMonthwiseProductionData(String year, String plantId);
    
    AOPMessageVM calculatePlantConsumptionSummaryReportData(String year, String plantId);
    AOPMessageVM calculateTurnAroundPlanReportData(String year, String plantId);
    AOPMessageVM calculateAnnualProductionPlanData(String year, String plantId);
    AOPMessageVM calculatePlantContributionReportData(String year, String plantId);

    
// --LoadMonthWiseProductionPlanReport
// --LoadPlantConsumptionSummaryReport
// --LoadTurnAroundPlanReport
// --LoadannualProductionPlan
// --LoadPlantContributionReport

    AOPMessageVM saveMonthWiseProductionPlanData(String plantId, String year, List<MonthWiseProductionPlanDTO> dataList);

    AOPMessageVM updateReportForMonthWiseConsumptionSummaryData(String plantId, String year, List<MonthWiseConsumptionSummaryDTO> dataList);

    
    AOPMessageVM savePlanTurnAroundData(String plantId, String year, List<TurnAroundPlanReportDTO> dataList);
}
