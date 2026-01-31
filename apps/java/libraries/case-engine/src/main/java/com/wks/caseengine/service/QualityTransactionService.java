package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.QualityTransactionDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface QualityTransactionService {
	
	public AOPMessageVM getQualityTransaction(String plantId,String year);
	public AOPMessageVM saveQualityTransaction( String year, String plantFKId, List<QualityTransactionDTO> qualityTransactionDTOs);
	public byte[] exportQualityTransaction(String year, String plantFKId,boolean isAfterSave,List<QualityTransactionDTO> dtoList);
	public AOPMessageVM importQualityTransaction(String year,UUID plantId,MultipartFile file);
}
