package com.wks.caseengine.service;

import java.util.UUID;

import com.wks.caseengine.message.vm.AOPMessageVM;

public interface DecokingActivitiesService {
	
	public AOPMessageVM getDecokingActivitiesData( String year, String plantId,String reportType);

}
