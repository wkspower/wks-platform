package com.wks.caseengine.service;

import java.util.List;
import com.wks.caseengine.dto.AOPConsumptionNormDTO;
import com.wks.caseengine.dto.CalculatedConsumptionNormsDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;


public interface AOPConsumptionNormService {
	
	public AOPMessageVM getAOPConsumptionNorm(String plantId,String year,String gradeId);
	public List<AOPConsumptionNormDTO> saveAOPConsumptionNorm(List<AOPConsumptionNormDTO> aOPConsumptionNormDTOList);
	public AOPMessageVM calculateExpressionConsumptionNorms(String year,String plantId);
	public List<CalculatedConsumptionNormsDTO> getCalculatedConsumptionNorms(String year, String plantId);

}
