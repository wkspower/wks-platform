package com.wks.caseengine.service;
import java.util.List;

import com.wks.caseengine.dto.AOPProposedNormsDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface AOPProposedNormsService {
	
	public AOPMessageVM getProposedNorms( String year,String plantId,String gradeId);
	public AOPMessageVM updateProposedNorms( String year,String plantId,List<AOPProposedNormsDTO> aopProposedNormsDTO);
	
}
