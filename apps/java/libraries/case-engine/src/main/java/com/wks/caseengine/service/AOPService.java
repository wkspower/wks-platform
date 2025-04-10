package com.wks.caseengine.service;
import java.util.List;

import com.wks.caseengine.dto.AOPDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface AOPService {
	
	public List<AOPDTO> getAOP();
	public AOPMessageVM updateAOP(List<AOPDTO> aOPDTOList);
	public AOPMessageVM getAOPData(String plantId, String year);
    public AOPMessageVM calculateData(String plantId, String year);
	List<Object[]> executeDynamicMaintenanceCalculation(String verticalName, String plantId, String siteId, String verticalId, String aopYear);

}
