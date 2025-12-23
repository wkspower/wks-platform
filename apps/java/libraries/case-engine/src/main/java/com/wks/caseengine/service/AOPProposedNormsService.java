package com.wks.caseengine.service;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface AOPProposedNormsService {
	
	public AOPMessageVM getProposedNorms( String year,String plantId,String gradeId);
	
}
