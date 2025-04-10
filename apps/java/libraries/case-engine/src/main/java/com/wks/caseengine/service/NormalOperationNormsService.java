package com.wks.caseengine.service;

import java.util.List;
import com.wks.caseengine.dto.MCUNormsValueDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface NormalOperationNormsService {
	
	public AOPMessageVM getNormalOperationNormsData( String year, String plantId);
	public AOPMessageVM saveNormalOperationNormsData( List<MCUNormsValueDTO> mCUNormsValueDTOList);
	public int calculateExpressionConsumptionNorms(String year,String plantId);
	// public int getCalculatedNormalOpsNorms( String year, String plantId);

}
