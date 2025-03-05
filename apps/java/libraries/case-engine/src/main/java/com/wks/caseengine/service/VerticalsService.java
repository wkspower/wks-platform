package com.wks.caseengine.service;

import java.util.List;

import com.wks.caseengine.dto.VerticalsDTO;


public interface VerticalsService {
	
	public List<VerticalsDTO> getAllVerticals();
	
	public List<VerticalsDTO> getAllVerticalsAndPlantsAndSites();

}
