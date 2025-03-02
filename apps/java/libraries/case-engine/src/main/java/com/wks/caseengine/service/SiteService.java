package com.wks.caseengine.service;
import java.util.List;

import com.wks.caseengine.dto.SitesDTO;
import com.wks.caseengine.entity.Sites;

public interface SiteService {
	
	public List<Sites> getAllSites();
	
	public List<Object[]> getAllSitesAndPlants();
	
	public List<SitesDTO> getSites();

}
