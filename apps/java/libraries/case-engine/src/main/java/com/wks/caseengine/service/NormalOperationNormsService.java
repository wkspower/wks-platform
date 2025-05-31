package com.wks.caseengine.service;

import java.util.List;
import com.wks.caseengine.dto.MCUNormsValueDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface NormalOperationNormsService {
	
	public AOPMessageVM getNormalOperationNormsData( String year, String plantId);
	public List<MCUNormsValueDTO> saveNormalOperationNormsData( List<MCUNormsValueDTO> mCUNormsValueDTOList);
	public AOPMessageVM calculateExpressionConsumptionNorms(String year,String plantId);
	
	AOPMessageVM getNormsTransaction(String plantId, String aopYear);
	
	// public int getCalculatedNormalOpsNorms( String year, String plantId);

}
