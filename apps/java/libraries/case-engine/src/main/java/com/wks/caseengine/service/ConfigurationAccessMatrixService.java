package com.wks.caseengine.service;

import com.wks.caseengine.message.vm.AOPMessageVM;

public interface ConfigurationAccessMatrixService {
	
	public AOPMessageVM getConfigurationAccessMatrix(String plantId, String siteId, String verticalId,String type);

}
