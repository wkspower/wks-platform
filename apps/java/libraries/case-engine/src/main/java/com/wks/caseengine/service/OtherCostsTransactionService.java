package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.OtherCostsTransactionDto;
import com.wks.caseengine.dto.QualityTransactionDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface OtherCostsTransactionService {
	
	public AOPMessageVM getOtherCostsTransaction(String plantId,String year);
	public AOPMessageVM saveOtherCostsTransaction( String year, String plantFKId, List<OtherCostsTransactionDto> otherCostsTransactionDTOs);
	public byte[] exportOtherCostsTransaction(String year, String plantFKId,boolean isAfterSave,List<OtherCostsTransactionDto> dtoList);
	public AOPMessageVM importOtherCostsTransaction(String year,UUID plantId,MultipartFile file);
}
