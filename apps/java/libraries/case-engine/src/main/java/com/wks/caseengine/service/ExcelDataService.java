package com.wks.caseengine.service;

import java.util.List;
import java.util.Map;

import com.wks.caseengine.message.vm.AOPMessageVM;

public interface ExcelDataService {

    List<List<Object>> getDataForProductionVolumeReport(String plantId, String year);

    List<List<Object>> getReportForMonthWiseProductionData(String plantId, String year);

    Map<String, List<List<Object>>> getReportForMonthWiseConsumptionSummaryData(String plantId, String year);

    Map<String, List<List<Object>>> getAnnualAOPReport(String plantId, String year);

    List<List<Object>> getReportForMonthWiseConsumptionForSelectivityData(String plantId, String year);

    List<List<Object>> getReportForTurnAroundPlanData(String plantId, String year, String reportType);

    List<List<Object>> getReportForPlantProductionPlanData(String plantId, String year, String reportType);

    List<List<Object>> getReportForPlantContributionYearWise(String plantId, String year, String reportType);

    List<List<Object>> getAOPData(String plantId, String year,String type);

    Map<String,Object> getProductionAOPWorkflowData(String plantId, String year);

    Map<String,Object> getAnnualAOPWorkflowData(String plantId, String year);
}
