package com.wks.caseengine.service;

import java.util.List;

import com.wks.caseengine.dto.SitesDTO;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface SiteService {

	public AOPMessageVM getAllSites();

	public AOPMessageVM getAllSitesAndPlants();

	public List<SitesDTO> getSites();

}
