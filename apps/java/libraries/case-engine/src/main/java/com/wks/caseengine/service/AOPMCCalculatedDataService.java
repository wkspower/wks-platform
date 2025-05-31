package com.wks.caseengine.service;

import java.util.List;

import org.springframework.web.bind.annotation.RequestParam;

import com.wks.caseengine.dto.AOPMCCalculatedDataDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface AOPMCCalculatedDataService {
	
	public  AOPMessageVM getAOPMCCalculatedData(String plantId, String year);
	public List<AOPMCCalculatedDataDTO> editAOPMCCalculatedData(List<AOPMCCalculatedDataDTO> aOPMCCalculatedDataDTOList);
	public  AOPMessageVM getAOPMCCalculatedDataSP(String plantId, String year);

}
