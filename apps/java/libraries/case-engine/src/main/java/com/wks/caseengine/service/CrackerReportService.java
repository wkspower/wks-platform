package com.wks.caseengine.service;



import com.wks.caseengine.message.vm.AOPMessageVM;

public interface CrackerReportService {
	
	public AOPMessageVM getSpyroInputReport( String plantId, String year, String mode);
	public AOPMessageVM getSpyroOutputReport( String plantId, String year, String mode);
	public AOPMessageVM getFinalNormsReport( String plantId, String year);
	public AOPMessageVM getFinalNormsProductionReport( String plantId, String year);
	public AOPMessageVM getConfigurationIntermediateValues(String plantId, String year);
	public AOPMessageVM getFindingModel(String plantId, String year);
	public AOPMessageVM getMIISData(String plantId, String year);
	
}
