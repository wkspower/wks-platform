package com.wks.caseengine.service;
import java.util.List;

import com.wks.caseengine.entity.CatalystAttributes;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface CatalystAttributesService {
	
	public List<CatalystAttributes> findAll();
	
	public AOPMessageVM getDummySpValues();
	
	

}
