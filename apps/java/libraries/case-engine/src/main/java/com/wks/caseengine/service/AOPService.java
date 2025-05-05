package com.wks.caseengine.service;
import java.util.List;
import java.util.Map;

import com.wks.caseengine.dto.AOPDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface AOPService {
	
	public List<AOPDTO> getAOP();
	public List<AOPDTO> updateAOP(List<AOPDTO> aOPDTOList);
	public List<AOPDTO> getAOPData(String plantId, String year);
    public AOPMessageVM calculateData(String plantId, String year);
    public List<Map<String, String>> getAOPYears();
	Integer executeDynamicMaintenanceCalculation(String verticalName, String plantId, String siteId, String verticalId, String aopYear);

}
