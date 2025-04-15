package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;

import com.wks.caseengine.dto.ShutDownPlanDTO;
import com.wks.caseengine.dto.SlowDownPlanDTO;

public interface SlowdownPlanService {

	public List<ShutDownPlanDTO> getPlans(UUID plantId, String type, String year);

	public List<ShutDownPlanDTO> savePlans(UUID plantId, List<ShutDownPlanDTO> shutDownPlanDTOList);

	public List<ShutDownPlanDTO> updatePlans(UUID transactionId, List<ShutDownPlanDTO> shutDownPlanDTOList);

}
