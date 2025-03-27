package com.wks.caseengine.service;

import java.util.List;
import com.wks.caseengine.dto.AOPConsumptionNormDTO;


public interface AOPConsumptionNormService {
	
	public List<AOPConsumptionNormDTO> getAOPConsumptionNorm(String plantId,String year);

}
