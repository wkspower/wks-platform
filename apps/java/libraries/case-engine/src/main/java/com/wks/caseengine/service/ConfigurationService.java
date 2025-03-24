package com.wks.caseengine.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.wks.caseengine.dto.ConfigurationDTO;



public interface ConfigurationService {
	
	public List<ConfigurationDTO> getConfigurationData(String year, UUID plantFKId);
    public String saveConfigurationData( String year,  List<ConfigurationDTO> configurationDTOList);

}
