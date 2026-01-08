package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;

import com.wks.caseengine.dto.TCSCPPUnitsSDPlanDTO;

public interface TCSCPPUnitsSDPlanService {

    List<TCSCPPUnitsSDPlanDTO> getTCSCPPUnitsSDPlan(String financialYear, UUID siteId);

    void saveTCSCPPUnitsSDPlan(List<TCSCPPUnitsSDPlanDTO> tcsCppUnitsSDPlanDTOs);
    
}
