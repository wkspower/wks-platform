package com.wks.caseengine.service;

import java.util.List;

import org.springframework.web.bind.annotation.RequestParam;

import com.wks.caseengine.dto.AOPMCCalculatedDataDTO;

public interface AOPMCCalculatedDataService {
	
	public  List<AOPMCCalculatedDataDTO> getAOPMCCalculatedData(String plantId, String year);
	public List<AOPMCCalculatedDataDTO> editAOPMCCalculatedData(List<AOPMCCalculatedDataDTO> aOPMCCalculatedDataDTOList);

}
