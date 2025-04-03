package com.wks.caseengine.service;

import java.util.List;
import com.wks.caseengine.dto.MCUNormsValueDTO;

public interface NormalOperationNormsService {
	
	public List<MCUNormsValueDTO> getNormalOperationNormsData( String year, String plantId);
	public List<MCUNormsValueDTO> saveNormalOperationNormsData( List<MCUNormsValueDTO> mCUNormsValueDTOList);
	public int calculateExpressionConsumptionNorms(String year,String plantId);
	public List<Object[]> getCalculatedNormalOpsNorms( String year, String plantId);

}
