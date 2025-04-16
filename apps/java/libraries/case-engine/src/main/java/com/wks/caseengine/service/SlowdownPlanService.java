package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;

import com.wks.caseengine.dto.ShutDownPlanDTO;
import com.wks.caseengine.dto.SlowDownPlanDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface SlowdownPlanService {

	public AOPMessageVM getPlans(UUID plantId, String type, String year);

	public AOPMessageVM savePlans(UUID plantId, List<ShutDownPlanDTO> shutDownPlanDTOList);

	public AOPMessageVM updatePlans(UUID transactionId, List<ShutDownPlanDTO> shutDownPlanDTOList);
}
