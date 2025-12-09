package com.wks.caseengine.service;

import java.util.List;

import com.wks.caseengine.dto.ConfigurationDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface QualityParametersService {
	
	public AOPMessageVM getQualityParameters(String plantId,String year);
	public AOPMessageVM saveQualityParameters( String year, String plantFKId, List<ConfigurationDTO> configurationDTOList);

}
