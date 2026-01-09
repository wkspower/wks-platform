package com.wks.caseengine.service;

import java.util.List;

import com.wks.caseengine.dto.ConfigurationDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface PriceDifferentialService {
	
	public AOPMessageVM getPriceDifferential(String plantId,String year);
	public AOPMessageVM savePriceDifferential( String year, String plantFKId, List<ConfigurationDTO> configurationDTOList);

}
