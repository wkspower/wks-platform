package com.wks.caseengine.service;

import java.util.Date;

import com.wks.caseengine.message.vm.AOPMessageVM;

public interface BasisReportService {
	
	public AOPMessageVM getNormBasisReportForPE( String plantId, String aopYear, String type,String periodFrom, String periodTo);

}
