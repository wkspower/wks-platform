package com.wks.caseengine.service;

import java.util.List;

import com.wks.caseengine.dto.ConfigurationDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface PackagingConsumablesService {
	
	public AOPMessageVM getPackagingConsumables(String plantId,String year);
	public AOPMessageVM savePackagingConsumables( String year, String plantFKId, List<ConfigurationDTO> configurationDTOList);

}
