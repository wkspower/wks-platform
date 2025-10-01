package com.wks.caseengine.service;

import java.util.List;
import java.util.Map;

public interface ExcelDataService {

    List<List<Object>> getDataForProductionVolumeReport(String plantId, String year, List<String> headers);

    List<List<Object>> getReportForMonthWiseProductionData(String plantId, String year, List<String> headers);

    Map<String, List<List<Object>>> getReportForMonthWiseConsumptionSummaryData(String plantId, String year,
            List<String> headers);

    Map<String, List<List<Object>>> getAnnualAOPReport(String plantId, String year);

    List<List<Object>> getReportForMonthWiseConsumptionForSelectivityData(String plantId, String year,
            List<String> headers);

    List<List<Object>> getReportForTurnAroundPlanData(String plantId, String year, String reportType,
            List<String> headers);

    List<List<Object>> getReportForPlantProductionPlanData(String plantId, String year, String reportType,
            List<String> headers);

    List<List<Object>> getReportForPlantContributionYearWise(String plantId, String year, String reportType,
            List<String> headers);

    List<List<Object>> getAOPData(String plantId, String year, String type);

    Map<String, Object> getProductionAOPWorkflowData(String plantId, String year, List<String> headers);

    List<List<Object>> getSpyroInputReport(String plantId, String year, String mode, List<String> headers);

    List<List<Object>> getSpyroOutputReport(String plantId, String year, String mode, List<String> headers);

    List<List<Object>> getFinalNormsProductionReport(String plantId, String year, String dataInput,
            List<String> headers);

    List<List<Object>> getMonthWiseRawDataByMethod(String plantId, String year, String mode, String method,
            List<String> headers);

    List<List<Object>> getFinalNormsReport(String plantId, String year, String dataInput, List<String> headers);

    List<List<Object>> getFurnaceReport(String plantId, String year, String dataInput,
            List<String> headers);

    Map<String, Object> getAnnualAOPWorkflowData(String plantId, String year, List<String> headers);

    List<List<Object>> getPlantContributionFiveYearSummaryReport(String plantId, String year, String reportType,
            List<String> headers);
}
