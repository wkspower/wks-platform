package com.wks.caseengine.service.cpp;

import java.util.List;
import java.util.UUID;

import com.wks.caseengine.dto.cpp.norm.NormsMonthUpdateRequestDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface NormBasedUtilityBudgetService {

    AOPMessageVM getNormBasedUtilityBudget(UUID cppPlantId, String financialYear);

    AOPMessageVM saveOrUpdate(NormsMonthUpdateRequestDTO dto, String financialYear, List<Object[]> remarkUpdates);

    AOPMessageVM saveOrUpdateBulk(List<NormsMonthUpdateRequestDTO> dtoList, String financialYear);

    
}
