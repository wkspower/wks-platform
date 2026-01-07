package com.wks.caseengine.service.cpp;
import java.util.List;
import java.util.UUID;

import com.wks.caseengine.dto.CalculatedProcessDemandDTO;
import com.wks.caseengine.dto.PlantRequirementDTO;
import com.wks.caseengine.dto.ProcessDemandUpdateRequest;
import com.wks.caseengine.dto.ProcessDemandUpdateResponse;

public interface ConsumptionService {
	
	public List<PlantRequirementDTO> getCppConsumptions(UUID plantId, String financialYear);

	public List<CalculatedProcessDemandDTO> getProcessDemand(String financialYear);

	public ProcessDemandUpdateResponse updateProcessDemand(String financialYear, List<ProcessDemandUpdateRequest> requests);
}
