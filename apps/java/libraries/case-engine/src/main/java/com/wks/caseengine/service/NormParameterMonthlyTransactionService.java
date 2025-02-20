package com.wks.caseengine.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface NormParameterMonthlyTransactionService {
	
	public	List<Map<String, Object>> getBusinessDemandData(int year, UUID plantId, UUID siteId);

}
