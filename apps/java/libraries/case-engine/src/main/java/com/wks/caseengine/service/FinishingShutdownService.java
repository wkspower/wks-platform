package com.wks.caseengine.service;

import java.util.List;

import com.wks.caseengine.dto.FinishingShutdownConfigDTO;

import com.wks.caseengine.message.vm.AOPMessageVM;

public interface FinishingShutdownService {

	
	public AOPMessageVM getFinishingShutdown(String plantId,String year);
	public AOPMessageVM saveFinishingShutdown( String year, String plantFKId, List<FinishingShutdownConfigDTO> finishingShutdownConfigDTOs);
	public AOPMessageVM deleteFinishingShutdown(String id);
}
