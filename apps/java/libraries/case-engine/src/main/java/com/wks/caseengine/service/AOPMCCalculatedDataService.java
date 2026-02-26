package com.wks.caseengine.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.AOPMCCalculatedDataDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface AOPMCCalculatedDataService {
	
	public  AOPMessageVM getAOPMCCalculatedData(String plantId, String year);
	public  AOPMessageVM getProductionTarget(String plantId, String year,String lineId);
	public  AOPMessageVM getMaxAchievedCapacity(String plantId, String year);
	public  AOPMessageVM getLineWiseMaxAchievedCapacity(String plantId, String year,String lineId);
	public  AOPMessageVM updateMaxAchievedCapacity(String plantId, String year,List<AOPMCCalculatedDataDTO> aopMCCalculatedDataDTOs);
	public  AOPMessageVM getDesignCapacity(String plantId, String year);
	public  AOPMessageVM getLineWiseDesignCapacity(String plantId, String year,String lineId);
	byte[] exportProductionTarget(String year, String plantId, boolean isAfterSave,
			Map<String, List<AOPMCCalculatedDataDTO>> mapForExcel);
	public AOPMessageVM getSummaryOfProposedOperating(String plantId, String year);
	public  AOPMessageVM updateDesignCapacity(String plantId, String year,List<AOPMCCalculatedDataDTO> aopMCCalculatedDataDTO);
	public List<AOPMCCalculatedDataDTO> editAOPMCCalculatedData(List<AOPMCCalculatedDataDTO> aOPMCCalculatedDataDTOList, boolean isFromExcel, String year, String plantFKId);
	public  AOPMessageVM getAOPMCCalculatedDataSP(String plantId, String year);
	public byte[] createExcel(String year, String plantFKId, boolean isAfterSave,List<AOPMCCalculatedDataDTO> dtoList);
	public AOPMessageVM importExcel(String year, String plantId, MultipartFile file);
	public AOPMessageVM importExcelPE(String year, String plantFKId, MultipartFile file);

}
