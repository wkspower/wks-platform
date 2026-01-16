package com.wks.caseengine.cpp.service;
import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.CalculatedProcessDemandDTO;
import com.wks.caseengine.dto.PlantRequirementDTO;
import com.wks.caseengine.dto.ProcessDemandUpdateRequest;
import com.wks.caseengine.dto.ProcessDemandUpdateResponse;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface ConsumptionService {
	
	public List<PlantRequirementDTO> getCppConsumptions(UUID plantId, String financialYear);

	public List<CalculatedProcessDemandDTO> getProcessDemand(String financialYear);

	public ProcessDemandUpdateResponse updateProcessDemand(String financialYear, List<ProcessDemandUpdateRequest> requests);

	public byte[] exportConsumption(UUID plantId, String financialYear, boolean isAfterSave, List<PlantRequirementDTO> dtoList);

	public AOPMessageVM importExcel(UUID plantId, String financialYear, MultipartFile file);
}


