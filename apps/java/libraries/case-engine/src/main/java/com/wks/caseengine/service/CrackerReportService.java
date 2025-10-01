package com.wks.caseengine.service;

import org.springframework.web.bind.annotation.RequestParam;

import com.wks.caseengine.message.vm.AOPMessageVM;

public interface CrackerReportService {
	
	public AOPMessageVM getSpyroInputReport( String plantId, String year, String mode);
	public AOPMessageVM getSpyroOutputReport( String plantId, String year, String mode);
	public AOPMessageVM getFinalNormsReport( String plantId, String year, String reportType);
	public AOPMessageVM getFinalNormsProductionReport( String plantId, String year, String reportType);
	public AOPMessageVM getConfigurationIntermediateValues(String plantId, String year);
	public AOPMessageVM getFindingModel(String plantId, String year);
	public AOPMessageVM getMIISData(String plantId, String year);
	public AOPMessageVM getCatChemRawDatasetReport( String plantId, String year,  String periodTo, String periodFrom);
	public AOPMessageVM getCatChemMonthlyAveragesReport( String plantId, String year,  String periodTo, String periodFrom);
	public AOPMessageVM getUtilitiesRawDataReport( String plantId, String year,  String periodTo, String periodFrom);
	public AOPMessageVM getSTGCatCamRawDatasetReport( String plantId, String year,  String periodTo, String periodFrom);
	public AOPMessageVM getMISUtiltiesMonthlyAveragesReport( String plantId, String year,  String periodTo, String periodFrom);
	public AOPMessageVM getRawDataForSteamValuesReport( String plantId, String year,  String periodTo, String periodFrom,String mode);
	public AOPMessageVM getFindingSteamValuesReport(String mode,String plantId,String year);
	public AOPMessageVM getFurnaceReport( String plantId, String year, String reportType);
	public AOPMessageVM getRunLengthDataSet( String plantId, String year, String reportType);
	public AOPMessageVM calculateMonthWiseRawData( String plantId, String year);
	public AOPMessageVM getMonthWiseRawDataByMethod(String plantId,String year,String mode,String method);
	
}
