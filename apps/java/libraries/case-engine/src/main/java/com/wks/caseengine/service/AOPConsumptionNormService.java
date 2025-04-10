package com.wks.caseengine.service;

import java.util.List;
import com.wks.caseengine.dto.AOPConsumptionNormDTO;
import com.wks.caseengine.dto.CalculatedConsumptionNormsDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;


public interface AOPConsumptionNormService {
	
	public AOPMessageVM getAOPConsumptionNorm(String plantId,String year);
	public AOPMessageVM saveAOPConsumptionNorm(List<AOPConsumptionNormDTO> aOPConsumptionNormDTOList);
	public int calculateExpressionConsumptionNorms(String year,String plantId);
	public AOPMessageVM getCalculatedConsumptionNorms(String year, String plantId);

}
