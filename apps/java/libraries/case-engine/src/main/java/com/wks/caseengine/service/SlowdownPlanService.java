package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;

public interface SlowdownPlanService {
	
	public List<Object[]> findSlowdownPlanDetailsByPlantIdAndType(UUID plantId,String maintenanceTypeName);

}
