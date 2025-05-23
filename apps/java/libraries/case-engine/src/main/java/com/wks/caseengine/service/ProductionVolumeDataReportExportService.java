package com.wks.caseengine.service;



public interface ProductionVolumeDataReportExportService {
	
	public byte[] getReportForPlantProductionPlanData(String plantId, String year, String reportType);

}
