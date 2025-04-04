package com.wks.caseengine.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


import com.wks.caseengine.dto.ConfigurationDTO;
import com.wks.caseengine.dto.NormAttributeTransactionReceipeDTO;
import com.wks.caseengine.entity.NormAttributeTransactionReceipe;



public interface ConfigurationService {
	
	public List<ConfigurationDTO> getConfigurationData(String year, UUID plantFKId);
    public List<ConfigurationDTO> saveConfigurationData( String year,  List<ConfigurationDTO> configurationDTOList);
    public  List<NormAttributeTransactionReceipeDTO>  getNormAttributeTransactionReceipe(String year, String plantId);
    public List<NormAttributeTransactionReceipe> NormAttributeTransactionReceipe(String year,String plantId,List<NormAttributeTransactionReceipeDTO> normAttributeTransactionReceipeDTOList);

}
