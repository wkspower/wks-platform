package com.wks.caseengine.service;

import java.util.List;

import com.wks.caseengine.dto.SpyroInputDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface SpyroInputService {
	
	public AOPMessageVM getSpyroInputData( String year, String plantId, String Mode);
	
	public AOPMessageVM updateSpyroInputData(  List<SpyroInputDTO> spyroInputDTOList);

}
