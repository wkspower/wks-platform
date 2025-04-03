package com.wks.caseengine.service;

import java.util.List;
import com.wks.caseengine.dto.AOPConsumptionNormDTO;
import com.wks.caseengine.dto.CalculatedConsumptionNormsDTO;


public interface AOPConsumptionNormService {
	
	public List<AOPConsumptionNormDTO> getAOPConsumptionNorm(String plantId,String year);
	public List<AOPConsumptionNormDTO> saveAOPConsumptionNorm(List<AOPConsumptionNormDTO> aOPConsumptionNormDTOList);
	public int calculateExpressionConsumptionNorms(String year,String plantId);
	public List<CalculatedConsumptionNormsDTO> getCalculatedConsumptionNorms(String year, String plantId);

}
