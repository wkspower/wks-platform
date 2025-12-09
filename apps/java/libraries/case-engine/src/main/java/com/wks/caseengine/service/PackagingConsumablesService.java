package com.wks.caseengine.service;

import com.wks.caseengine.message.vm.AOPMessageVM;

public interface PackagingConsumablesService {
	
	public AOPMessageVM getPackagingConsumables(String plantId,String year);

}
