package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;


import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.AOPMCCalculatedDataDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface AOPMCCalculatedDataService {
	
	public  AOPMessageVM getAOPMCCalculatedData(String plantId, String year);
	public List<AOPMCCalculatedDataDTO> editAOPMCCalculatedData(List<AOPMCCalculatedDataDTO> aOPMCCalculatedDataDTOList, boolean isFromExcel, String year, String plantFKId);
	public  AOPMessageVM getAOPMCCalculatedDataSP(String plantId, String year);
	public byte[] createExcel(String year, String plantFKId, boolean isAfterSave,List<AOPMCCalculatedDataDTO> dtoList);
	public AOPMessageVM importExcel(String year, String plantId, MultipartFile file);

}
