package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.AOPConsumptionNormDTO;
import com.wks.caseengine.dto.NormAttributeTransactionsDTO;
import com.wks.caseengine.dto.ShutdownNormsValueDTO;
import com.wks.caseengine.dto.SlowdownNormsValueDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface SlowdownNormsService {
	
	public AOPMessageVM getSlowdownNormsData( String year, String plantId,String gradeId);
	public byte[] exportSlowdownNorms(String year, UUID plantFKId,boolean isAfterSave,List<SlowdownNormsValueDTO> dtoList);
	public byte[] exportSlowdownConsumption(String year, UUID plantFKId,boolean isAfterSave,List<SlowdownNormsValueDTO> dtoList,String gradeId);
	public AOPMessageVM importSlowdownConsumption(String year, UUID fromString,String gradeId, MultipartFile file);
	public List<SlowdownNormsValueDTO> saveSlowdownNormsData( List<SlowdownNormsValueDTO> slowdownNormsValueDTOList);
	public List<SlowdownNormsValueDTO> getSlowdownNormsSPData(String year, String plantId);
	public List getSlowdownMonths(UUID plantId,String maintenanceName,String year,String gradeId);
	public List getSlowdownMonthsImport(UUID plantId,String maintenanceName,String year);
	public AOPMessageVM getCalculateSlowdownNorms(String year,String plantId);
	public AOPMessageVM calculateSlowdownNorms(String year,String plantId);
	public AOPMessageVM getSlowdownNormsDynamicColumns(String auditYear,  UUID plantId);
	public AOPMessageVM getSlowdownNormsConfigurationData(String plantId,String year);
	public AOPMessageVM saveSlowdownNormsConfigurationData( String plantId, String year,  List<NormAttributeTransactionsDTO> normAttributeTransactionsDTOList);
	public AOPMessageVM getUniqueGrades(String year, String plantId);


}
