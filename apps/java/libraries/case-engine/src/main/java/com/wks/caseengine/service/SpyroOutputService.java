package com.wks.caseengine.service;

import java.util.List;

import com.wks.caseengine.dto.NormAttributeTransactionsDTO;
import com.wks.caseengine.dto.SpyroOutputDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface SpyroOutputService {
	
	public AOPMessageVM getSpyroOutputData( String year, String plantId, String Mode,String type);
	
	public AOPMessageVM updateSpyroOutputData(String year,String plantId,  List<SpyroOutputDTO> spyroOutputDTOList);
	
	public AOPMessageVM getSpyroOutputYieldData( String year, String plantId);
	
	public AOPMessageVM updateSpyroOutputYieldData( String plantId, String year,  List<NormAttributeTransactionsDTO> normAttributeTransactionsDTOList);
	

}
