package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.ConfigurationDTO;
import com.wks.caseengine.dto.OtherCostsTransactionDto;
import com.wks.caseengine.dto.PackagingAndConsumableTransactionDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface PackagingConsumablesService {
	
	public AOPMessageVM getPackagingConsumables(String plantId,String year);
	public AOPMessageVM savePackagingConsumables( String year, String plantFKId, List<ConfigurationDTO> configurationDTOList);
	public AOPMessageVM getPackagingConsumablesTransaction(String plantId,String year);
	public AOPMessageVM savePackagingConsumablesTransaction( String year, String plantFKId, List<PackagingAndConsumableTransactionDTO> packagingAndConsumableTransactionDTOs);
	public byte[] exportPackagingConsumablesTransaction(String year, String plantFKId,boolean isAfterSave,List<PackagingAndConsumableTransactionDTO> dtoList);
	public AOPMessageVM importPackagingConsumablesTransaction(String year,UUID plantId,MultipartFile file);

}
