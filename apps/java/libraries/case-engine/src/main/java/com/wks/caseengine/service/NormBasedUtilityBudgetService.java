package com.wks.caseengine.service;

import java.util.UUID;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface NormBasedUtilityBudgetService {

    AOPMessageVM getNormBasedUtilityBudget(UUID cppPlantId, String financialYear);
}
