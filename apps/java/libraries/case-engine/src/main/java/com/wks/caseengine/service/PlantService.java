package com.wks.caseengine.service;
import java.util.List;
import java.util.UUID;

import org.springframework.data.repository.query.Param;

import com.wks.caseengine.rest.entity.Plant;

public interface PlantService {
	
	public List<Plant> getPlantBySite(String siteId);
	public List<Object[]> getPlantAndSite();
	public List getShutdownMonths(UUID plantId,String maintenanceName);
	

}
