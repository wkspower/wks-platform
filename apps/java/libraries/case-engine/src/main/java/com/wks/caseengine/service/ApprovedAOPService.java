package com.wks.caseengine.service;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface ApprovedAOPService {
	
	public AOPMessageVM getApprovedAOP(String plantId, String year);
	public AOPMessageVM updateApprovedAOP(String plantId, String year);
	public AOPMessageVM deleteApprovedAOP(String id);
	
}
