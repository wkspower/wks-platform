package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.ConfigurationDTO;
import com.wks.caseengine.dto.PriceDifferentialTransactionDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface PriceDifferentialService {
	
	public AOPMessageVM getPriceDifferential(String plantId,String year);
	public AOPMessageVM getPriceDifferentialTransaction(String plantId,String year);
	public AOPMessageVM savePriceDifferential( String year, String plantFKId, List<ConfigurationDTO> configurationDTOList);
	public AOPMessageVM savePriceDifferentialTransaction( String year, String plantFKId, List<PriceDifferentialTransactionDTO> priceDifferentialTransactionDTOs);
	public byte[] exportPriceDifferentialTransaction(String year, String plantFKId,boolean isAfterSave,List<PriceDifferentialTransactionDTO> dtoList);
	public AOPMessageVM importPriceDifferentialTransaction(String year,UUID plantId,MultipartFile file);
}
