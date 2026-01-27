package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.ExclusionDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface ExclusionService {
	
	public AOPMessageVM getExclusionDate(String plantId,String year);
	public AOPMessageVM deleteExclusionDate(String id);
	public AOPMessageVM saveExclusionDate( String year, String plantFKId, List<ExclusionDTO> exclusionDTOs);
	public byte[] exportExclusionDate(String year, String plantFKId,boolean isAfterSave,List<ExclusionDTO> dtoList);
	public AOPMessageVM importExclusionDate(String year,UUID plantId,MultipartFile file);
}
