package com.wks.caseengine.service;

import java.util.List;

import com.wks.caseengine.dto.SpyroOutputDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface SpyroOutputService {
	
	public AOPMessageVM getSpyroOutputData( String year, String plantId, String Mode);
	
	public AOPMessageVM updateSpyroOutputData(  List<SpyroOutputDTO> spyroOutputDTOList);

}
