package com.wks.caseengine.service;

import java.util.List;

import com.wks.caseengine.dto.DecokingActivitiesDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface DecokingActivitiesService {
	
	public AOPMessageVM getDecokingActivitiesData( String year, String plantId,String reportType);
	public AOPMessageVM updateDecokingActivitiesData( String year, String plantId, String reportType, List<DecokingActivitiesDTO> decokingActivitiesDTOList);

}
