package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;

import com.wks.caseengine.dto.BusinessDemandDataDTO;

public interface BusinessDemandDataService {
	public	List<BusinessDemandDataDTO> getBusinessDemandData(String year, UUID plantId);
	public BusinessDemandDataDTO saveBusinessDemandData(BusinessDemandDataDTO businessDemandDataDTO);

}
