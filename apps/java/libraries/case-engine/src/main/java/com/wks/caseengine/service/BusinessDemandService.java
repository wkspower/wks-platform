package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;

import com.wks.caseengine.dto.BusinessDemandDataDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface BusinessDemandService {
	public	AOPMessageVM getBusinessDemandData(String year, String plantId);
	public 	AOPMessageVM  saveBusinessDemandData(List<BusinessDemandDataDTO> businessDemandDataDTO);
	public 	List<BusinessDemandDataDTO>  editBusinessDemandData(List<BusinessDemandDataDTO> businessDemandDataDTO);
	public 	BusinessDemandDataDTO  deleteBusinessDemandData(UUID id);

}
