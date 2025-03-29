package com.wks.caseengine.service;
import java.util.List;
import java.util.UUID;

import com.wks.caseengine.dto.PlantsDTO;

public interface PlantsService {
	
	public List<PlantsDTO> getAllPlants();
	public String findVerticalNameByPlantId(UUID plantId);

}
