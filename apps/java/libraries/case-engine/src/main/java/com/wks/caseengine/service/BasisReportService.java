package com.wks.caseengine.service;



import org.springframework.web.bind.annotation.RequestParam;

import com.wks.caseengine.message.vm.AOPMessageVM;

public interface BasisReportService {
	
	public AOPMessageVM getNormBasisReportForPE( String plantId, String aopYear, String type,String periodFrom, String periodTo);
	public AOPMessageVM getNormBasisReportCracker( String plantId, String aopYear, String type,String mode);
	public AOPMessageVM getBestAchievedCracker( String plantId, String aopYear, String reportType);
	public AOPMessageVM calculateBestAchieved( String year, String plantId,String periodTo, String periodFrom);

}
