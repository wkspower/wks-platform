package com.wks.caseengine.service;

import com.wks.caseengine.message.vm.AOPMessageVM;

public interface BasisReportService {
	
	public AOPMessageVM getNormBasisReport( String plantId, String aopYear, String type);

}
