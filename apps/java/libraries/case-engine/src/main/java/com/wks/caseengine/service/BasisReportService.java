package com.wks.caseengine.service;

import com.wks.caseengine.message.vm.AOPMessageVM;

public interface BasisReportService {
	
	public AOPMessageVM getNormhistorian(String plantId, String aopYear, String periodFrom, String periodTo,String type);
	        
	public AOPMessageVM getNormBasisReportCracker( String plantId, String aopYear, String type,String mode);
	public AOPMessageVM getBestAchievedCracker( String plantId, String aopYear, String reportType);
	public AOPMessageVM calculateBestAchieved( String year, String plantId,String periodTo, String periodFrom);
	public AOPMessageVM calculateBestAchievedIndividual(String year, String plantId, String periodTo,
			String periodFrom);

}
