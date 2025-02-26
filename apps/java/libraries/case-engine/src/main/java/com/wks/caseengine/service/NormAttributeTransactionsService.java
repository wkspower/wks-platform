package com.wks.caseengine.service;

import java.util.List;
import java.util.Map;
import com.wks.caseengine.dto.NormAttributeTransactionsDTO;

public interface NormAttributeTransactionsService {
	
	public String getCatalystSelectivityData(int year);
	public NormAttributeTransactionsDTO updateNormAttributeTransactions(NormAttributeTransactionsDTO normAttributeTransactionsDTO);

}
