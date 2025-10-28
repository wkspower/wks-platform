package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.BusinessDemandDataDTO;
import com.wks.caseengine.dto.BusinessDemandMonthlyDTO;
import com.wks.caseengine.dto.ConfigurationDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface BusinessDemandDataService {
	public	List<BusinessDemandDataDTO> getBusinessDemandData(String year, String plantId);
	public AOPMessageVM getBusinessDemand(String year, UUID plantFKId);
	public AOPMessageVM saveBusinessDemand( String year, String plantFKId, List<BusinessDemandMonthlyDTO> businessDemandMonthlyDTOs);
	public byte[] exportBusinessDemand(String year, String plantFKId,boolean isAfterSave,List<BusinessDemandDataDTO> dtoList);
	public 	List<BusinessDemandDataDTO>  saveBusinessDemandData(List<BusinessDemandDataDTO> businessDemandDataDTO);
	public 	List<BusinessDemandDataDTO>  editBusinessDemandData(List<BusinessDemandDataDTO> businessDemandDataDTO);
	public 	BusinessDemandDataDTO  deleteBusinessDemandData(UUID id);
	public AOPMessageVM importExcel(String year, UUID plantId, MultipartFile file);

}
