package com.wks.caseengine.service;

import java.util.List;
import com.wks.caseengine.dto.ConsumptionNormDTO;
import com.wks.caseengine.dto.CalculatedConsumptionNormsDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;


public interface ConsumptionNormService {
	
	public AOPMessageVM getAOPConsumptionNorm(String plantId,String year);
	public AOPMessageVM saveAOPConsumptionNorm(List<ConsumptionNormDTO> aOPConsumptionNormDTOList);
	public int calculateExpressionConsumptionNorms(String year,String plantId);
	public AOPMessageVM getCalculatedConsumptionNorms(String year, String plantId);

}
