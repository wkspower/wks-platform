package com.wks.caseengine.service;
import java.util.List;
import com.wks.caseengine.rest.entity.Plant;

public interface PlantService {
	
	public List<Plant> getPlantBySite(String siteId);

}
