package com.wks.caseengine.service;
import java.util.List;
import java.util.UUID;

import com.wks.caseengine.dto.PlantRequirementDTO;

public interface ConsumptionService {
	
	public List<PlantRequirementDTO> getCppConsumptions(UUID plantId);

}
