package com.wks.caseengine.cpp.service;
import java.util.List;
import java.util.UUID;

import com.wks.caseengine.cpp.dto.CalculatedProcessDemandDTO;
import com.wks.caseengine.cpp.dto.PlantRequirementDTO;
import com.wks.caseengine.cpp.dto.ProcessDemandUpdateRequest;
import com.wks.caseengine.cpp.dto.ProcessDemandUpdateResponse;

public interface ConsumptionService {
	
	public List<PlantRequirementDTO> getCppConsumptions(UUID plantId, String financialYear);

	public List<CalculatedProcessDemandDTO> getProcessDemand(String financialYear);

	public ProcessDemandUpdateResponse updateProcessDemand(String financialYear, List<ProcessDemandUpdateRequest> requests);
}


