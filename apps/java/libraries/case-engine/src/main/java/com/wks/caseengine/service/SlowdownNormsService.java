package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;

import com.wks.caseengine.dto.NormAttributeTransactionsDTO;
import com.wks.caseengine.dto.SlowdownNormsValueDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface SlowdownNormsService {
	
	public List<SlowdownNormsValueDTO> getSlowdownNormsData( String year, String plantId);
	public List<SlowdownNormsValueDTO> saveSlowdownNormsData( List<SlowdownNormsValueDTO> slowdownNormsValueDTOList);
	public List<SlowdownNormsValueDTO> getSlowdownNormsSPData(String year, String plantId);
	public List getSlowdownMonths(UUID plantId,String maintenanceName,String year);
	public AOPMessageVM getCalculateSlowdownNorms(String year,String plantId);
	public AOPMessageVM getSlowdownNormsDynamicColumns(String auditYear,  UUID plantId);
	public AOPMessageVM getSlowdownNormsConfigurationData(String plantId,String year);
	public AOPMessageVM saveSlowdownNormsConfigurationData( String plantId, String year,  List<NormAttributeTransactionsDTO> normAttributeTransactionsDTOList);
	


}
