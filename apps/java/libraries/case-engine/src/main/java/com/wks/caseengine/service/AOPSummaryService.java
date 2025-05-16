package com.wks.caseengine.service;

import com.wks.caseengine.dto.AOPSummaryDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface AOPSummaryService {
    AOPMessageVM saveAOPSummary(String plantId, String aopYear, AOPSummaryDTO aopSummaryDTO);

    AOPMessageVM getAOPSummary(String plantId, String aopYear);

}
