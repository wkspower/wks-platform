package com.wks.caseengine.service;

import java.util.List;

import com.wks.caseengine.dto.BusinessDemandDataDTO;

public interface BusinessDemandDataService {
	public	List<BusinessDemandDataDTO> getBusinessDemandData(String year, String plantId);
	public 	List<BusinessDemandDataDTO>  saveBusinessDemandData(List<BusinessDemandDataDTO> businessDemandDataDTO);
	public 	List<BusinessDemandDataDTO>  editBusinessDemandData(List<BusinessDemandDataDTO> businessDemandDataDTO);
	public 	BusinessDemandDataDTO  deleteBusinessDemandData(BusinessDemandDataDTO businessDemandDataDTO);

}
