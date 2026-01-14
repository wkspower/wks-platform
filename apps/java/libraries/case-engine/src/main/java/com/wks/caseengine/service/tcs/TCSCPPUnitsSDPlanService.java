package com.wks.caseengine.service.tcs;

import java.util.List;
import java.util.UUID;

import com.wks.caseengine.dto.tcs.TCSCPPUnitsSDPlanDTO;

public interface TCSCPPUnitsSDPlanService {

    List<TCSCPPUnitsSDPlanDTO> getTCSCPPUnitsSDPlan(String financialYear, UUID siteId);

    void saveTCSCPPUnitsSDPlan(List<TCSCPPUnitsSDPlanDTO> tcsCppUnitsSDPlanDTOs, UUID siteId, String financialYear);

    void deleteTCSCPPUnitsSDPlan(UUID id);
    
}
