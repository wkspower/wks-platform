package com.wks.caseengine.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;


import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.ConfigurationDTO;
import com.wks.caseengine.dto.ExecutionDetailDto;
import com.wks.caseengine.dto.NormAttributeTransactionReceipeDTO;
import com.wks.caseengine.dto.NormAttributeTransactionReceipeRequestDTO;
import com.wks.caseengine.entity.NormAttributeTransactionReceipe;
import com.wks.caseengine.message.vm.AOPMessageVM;



public interface ConfigurationService {
	
	public List<ConfigurationDTO> getConfigurationData(String year, UUID plantFKId);
	public AOPMessageVM getConfigurationConstants(String year,String plantFKId);
	public AOPMessageVM getConfigurationIntermediateValues(String year, UUID plantFKId);
    public List<ConfigurationDTO> saveConfigurationData( String year, String plantFKId, List<ConfigurationDTO> configurationDTOList);
    public   List<Map<String, Object>>  getNormAttributeTransactionReceipe(String year, String plantId);
    public List<NormAttributeTransactionReceipe> updateCalculatedConsumptionNorms( String year, String plantId,  List<NormAttributeTransactionReceipeRequestDTO> normAttributeTransactionReceipeDTOLists);
    public AOPMessageVM getConfigurationIntermediateValuesData(String year, String plantId);
    public byte[] createExcel(String year, UUID plantFKId, boolean isAfterSave, List<ConfigurationDTO> list);
    public byte[] createConfigurationConstantsExcel(String year, UUID plantFKId);
    public byte[] importExcel(String year, UUID fromString, MultipartFile file);
    public byte[] importConfigurationConstantsExcel(String year, UUID plantId, MultipartFile file);
	public AOPMessageVM getConfigurationExecution( String year, String plantId);
    public AOPMessageVM saveConfigurationExecution( List<ExecutionDetailDto> executionDetailDtoList);
    byte[] createConfigurationConstantsExcelResponse(String year, UUID plantFKId, List<ConfigurationDTO> list);

}
