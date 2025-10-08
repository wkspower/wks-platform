package com.wks.caseengine.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;


import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.ConfigurationDTO;
import com.wks.caseengine.dto.ExecutionDetailDto;
import com.wks.caseengine.dto.NormAttributeTransactionReceipeRequestDTO;
import com.wks.caseengine.entity.NormAttributeTransactionReceipe;
import com.wks.caseengine.message.vm.AOPMessageVM;



public interface ConfigurationService {
	
	public List<ConfigurationDTO> getConfigurationData(String year, UUID plantFKId);
	AOPMessageVM calculateSteadyNorms(String year, String plantId,String periodTo,String periodFrom);
	public AOPMessageVM getConfigurationConstants(String year,String plantFKId);
	public AOPMessageVM getConfigurationIntermediateValues(String year, UUID plantFKId);
    public List<ConfigurationDTO> saveConfigurationData( String year, String plantFKId, List<ConfigurationDTO> configurationDTOList);
    public   List<Map<String, Object>>  getNormAttributeTransactionReceipe(String year, String plantId);
    public List<NormAttributeTransactionReceipeRequestDTO> updateCalculatedConsumptionNorms( String year, String plantId,  List<NormAttributeTransactionReceipeRequestDTO> normAttributeTransactionReceipeDTOLists);
    public AOPMessageVM getConfigurationIntermediateValuesData(String year, String plantId);
    public byte[] createExcel(String year, UUID plantFKId, String reportType,boolean isAfterSave, List<ConfigurationDTO> list);
    public byte[] createShutdownRateExcel(String year, UUID plantFKId, boolean isAfterSave, List<ConfigurationDTO> list);
    public byte[] createConfigurationConstantsExcel(String year, UUID plantFKId);
    public byte[] exportConfigData(String year, UUID plantFKId, boolean isAfterSave, List<NormAttributeTransactionReceipeRequestDTO> dtoList);
    public AOPMessageVM importExcel(String year, UUID fromString,String reportType, MultipartFile file);
    public AOPMessageVM importShutdownRateExcel(String year, UUID fromString, MultipartFile file);
    public AOPMessageVM importRecipe(String year, UUID fromString, MultipartFile file);
    public AOPMessageVM importConfigurationConstantsExcel(String year, UUID plantId, MultipartFile file);
	public AOPMessageVM getConfigurationExecution( String year, String plantId);
	public AOPMessageVM getConfigurationExecutionNorms( String year, String plantId);
    public AOPMessageVM saveConfigurationExecution( List<ExecutionDetailDto> executionDetailDtoList);
    public AOPMessageVM saveConfigurationExecutionNorms( List<ExecutionDetailDto> executionDetailDtoList);
    byte[] createConfigurationConstantsExcelResponse(String year, UUID plantFKId, List<ConfigurationDTO> list);
    public AOPMessageVM getConfigurationConstantsNorms(String year, String plantFKId);

}
