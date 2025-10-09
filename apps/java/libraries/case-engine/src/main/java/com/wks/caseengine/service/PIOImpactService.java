package com.wks.caseengine.service;


import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.PIOImpactDTO;

import com.wks.caseengine.message.vm.AOPMessageVM;

public interface PIOImpactService {

	
	AOPMessageVM getPIOImpact(String year, String plantId);
	
	AOPMessageVM updatePIOImpact(String year, String plantId,List<PIOImpactDTO> pioImpactDTOs);
	
	AOPMessageVM deletePIOImpact(UUID id);
	
	public byte[] exportPIOImpact(String year, String plantFKId,boolean isAfterSave,List<PIOImpactDTO> dtoList);
	public AOPMessageVM importPIOImpact(String year,UUID plantId,MultipartFile file);
}
