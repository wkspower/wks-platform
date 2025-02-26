package com.wks.caseengine.service;
import java.util.List;

import com.wks.caseengine.dto.AOPDTO;

public interface AOPService {
	
	public List<AOPDTO> getAOP();
	public AOPDTO updateAOP(AOPDTO aOPDTO);

}
