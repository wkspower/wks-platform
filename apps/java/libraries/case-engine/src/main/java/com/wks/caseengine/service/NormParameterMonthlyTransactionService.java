package com.wks.caseengine.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.wks.caseengine.dto.BusinessDemandDTO;

public interface NormParameterMonthlyTransactionService {
	
	public String getBusinessDemandData(int year, UUID plantId, UUID siteId);

	public String getProductionNormData(int year, UUID plantId, UUID siteId);

	public String getCosnumptionNormData(int year, UUID plantId, UUID siteId);

    public void saveBusinessDemandData(UUID plantId, BusinessDemandDTO businessDemandDTO);

    public void editBusinessDemandData(UUID plantMaintenanceTransactionId, BusinessDemandDTO businessDemandDTO);

    public void deleteBusinessDemandData(UUID plantMaintenanceTransactionId);
	

}
