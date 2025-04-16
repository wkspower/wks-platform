package com.wks.caseengine.service;
import java.util.List;
import java.util.Map;

import com.wks.caseengine.dto.AOPDTO;

public interface AOPService {
	
	public List<AOPDTO> getAOP();
	public List<AOPDTO> updateAOP(List<AOPDTO> aOPDTOList);
	public List<AOPDTO> getAOPData(String plantId, String year);
    public List<AOPDTO> calculateData(String plantId, String year);
    public List getAOPYears();
	List<Object[]> executeDynamicMaintenanceCalculation(String verticalName, String plantId, String siteId, String verticalId, String aopYear);

}
