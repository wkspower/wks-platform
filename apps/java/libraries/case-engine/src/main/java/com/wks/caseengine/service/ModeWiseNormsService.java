package com.wks.caseengine.service;


import java.util.List;

import com.wks.caseengine.dto.ModeWiseNormsDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface ModeWiseNormsService {

	
	AOPMessageVM getModeWiseNormsData(String year, String plantId, String Mode, String Method);
	
	AOPMessageVM updateModeWiseNormsData(String year, String plantId, String Mode, String Method,List<ModeWiseNormsDTO> modeWiseNormsDTOList);

}
