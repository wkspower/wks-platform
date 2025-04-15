package com.wks.caseengine.service;

import java.util.Map;

public interface UserScreenMappingService {
	
	public Map<String, Object> getUserScreenMapping(String verticalId, String plantId, String userId) throws Exception;

}
