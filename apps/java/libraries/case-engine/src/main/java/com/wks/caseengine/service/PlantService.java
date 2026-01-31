package com.wks.caseengine.service;
import java.util.List;
import java.util.UUID;

import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.rest.entity.Plant;

public interface PlantService {
	
	public List<Plant> getPlantBySite(String siteId);
	public List<Object[]> getPlantAndSite();
	public List getShutdownMonths(UUID plantId,String maintenanceName,String year,String gradeId);
	public List<Plants> findUniqueNamesPlantsByVerticalAndSite(UUID verticalId, UUID siteId);
	

}
