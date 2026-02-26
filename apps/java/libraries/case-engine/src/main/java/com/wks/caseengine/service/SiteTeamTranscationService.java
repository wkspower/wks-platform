package com.wks.caseengine.service;

import java.util.List;
import com.wks.caseengine.dto.SiteTeamTranscationDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface SiteTeamTranscationService {
	
	public AOPMessageVM getSiteTeamTransaction(String siteId,String year);
	public AOPMessageVM saveSiteTeamTransaction( String year, String plantFKId, List<SiteTeamTranscationDTO> siteTeamTranscationDTOs);
	
}
