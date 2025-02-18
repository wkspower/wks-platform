package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;

public interface ShutDownPlanService {
	
	public List<Object[]> findMaintenanceDetailsByPlantIdAndType(UUID plantId,String maintenanceTypeName);

}
