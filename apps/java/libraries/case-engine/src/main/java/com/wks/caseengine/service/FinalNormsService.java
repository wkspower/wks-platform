package com.wks.caseengine.service;


import java.util.List;

import com.wks.caseengine.dto.ModeWiseNormsDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface FinalNormsService {

	
	AOPMessageVM getFinalNorms(String year, String plantId, String Mode, String Method);
	
	AOPMessageVM updateFinalNorms(String year, String plantId, String Mode, String Method,List<ModeWiseNormsDTO> modeWiseNormsDTOList);
	
	AOPMessageVM calculateFinalNorms(String year, String plantId, String Mode, String Method);
}
