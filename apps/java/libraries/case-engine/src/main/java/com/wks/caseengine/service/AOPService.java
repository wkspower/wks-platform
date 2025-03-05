package com.wks.caseengine.service;
import java.util.List;

import com.wks.caseengine.dto.AOPDTO;

public interface AOPService {
	
	public List<AOPDTO> getAOP();
	public List<AOPDTO> updateAOP(List<AOPDTO> aOPDTOList);
	public List<AOPDTO> getAOPData(String plantId, String year);
    public List<AOPDTO> calculateData(String plantId, String year);

}
