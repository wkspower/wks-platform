package com.wks.caseengine.service;

import com.wks.caseengine.message.vm.AOPMessageVM;

public interface ReliabilityService {
	
	
	public AOPMessageVM getReliabilityPerformance(String plantId, String year,String type);
	public AOPMessageVM getReliabilityRecords(String plantId, String year,String type);
	
}
