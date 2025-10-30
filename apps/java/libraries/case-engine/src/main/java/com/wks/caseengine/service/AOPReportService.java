package com.wks.caseengine.service;

import java.util.List;

import com.wks.caseengine.dto.PlantContributionSummaryDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface AOPReportService {
	
	public AOPMessageVM getAnnualAOPReport( String plantId, String year, String reportType,String AopYearFilter);
	public AOPMessageVM getReportForProductionVolumnData(String plantId, String year, String reportType,String uom);
	public AOPMessageVM getHandleCalculateMIISContribution(String plantId, String year);
	public AOPMessageVM getFiveYearSummaryReport( String plantId, String year, String reportType);
	public AOPMessageVM getPlantContributionFiveYearSummaryReport( String reportType, String plantId, String year);
	public AOPMessageVM getSpecificConsumptionNormsReport( String reportType, String plantId, String year);
	public AOPMessageVM updatePlantContributionFiveYearSummaryReport( List<PlantContributionSummaryDTO> plantContributionSummaryDTO);
}
