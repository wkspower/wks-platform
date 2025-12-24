package com.wks.caseengine.service;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface ApprovedAOPService {
	
	//public AOPMessageVM getProposedNorms( String year,String plantId,String gradeId);
	public AOPMessageVM updateApprovedAOP(String plantId, String year);
	
}
