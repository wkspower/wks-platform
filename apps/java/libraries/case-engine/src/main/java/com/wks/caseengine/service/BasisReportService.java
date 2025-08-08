package com.wks.caseengine.service;



import com.wks.caseengine.message.vm.AOPMessageVM;

public interface BasisReportService {
	
	public AOPMessageVM getNormBasisReportForPE( String plantId, String aopYear, String type,String periodFrom, String periodTo);
	public AOPMessageVM getNormBasisReportCracker( String plantId, String aopYear, String type,String mode);

}
