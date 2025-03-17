package com.wks.caseengine.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;



public interface ConfigurationService {
	
	public List<Map<String, Object>> getConfigurationData(String year,UUID plantFKId);

}
